package com.smartexpense.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

import com.smartexpense.dto.request.CreateTripRequest;
import com.smartexpense.dto.request.InviteMemberRequest;
import com.smartexpense.dto.response.MemberResponse;
import com.smartexpense.dto.response.TripResponse;
import com.smartexpense.dto.websocket.TripStatusEvent;
import com.smartexpense.model.Trip;
import com.smartexpense.model.TripMember;
import com.smartexpense.model.User;
import com.smartexpense.model.enums.MemberRole;
import com.smartexpense.model.enums.TripStatus;
import com.smartexpense.model.Expense;
import com.smartexpense.repository.ExpenseRepository;
import com.smartexpense.repository.PaymentRepository;
import com.smartexpense.repository.TripMemberRepository;
import com.smartexpense.repository.TripRepository;
import com.smartexpense.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TripService {

	private final TripRepository tripRepository;
	private final TripMemberRepository tripMemberRepository;
	private final UserRepository userRepository;
	private final PaymentRepository paymentRepository;
	private final ExpenseRepository expenseRepository;
	private final TripEventPublisher eventPublisher;

	@Transactional
	public TripResponse createTrip(CreateTripRequest request, String organizerUserId) {
		User organizer = userRepository.findById(organizerUserId)
				.orElseThrow(() -> new RuntimeException("User not found"));

		Trip trip = Trip.builder()
				.name(request.getName())
				.destination(request.getDestination())
				.startDate(request.getStartDate())
				.endDate(request.getEndDate())
				.status(TripStatus.PLANNING)
				.organizerId(organizerUserId)
				.kittyTarget(request.getKittyTarget() != null ? request.getKittyTarget() : 0.0)
				.kittyBalance(0.0)
				.baseCurrency(StringUtils.hasText(request.getBaseCurrency()) ? request.getBaseCurrency() : "INR")
				.build();

		trip = tripRepository.save(trip);

		TripMember organizerMember = TripMember.builder()
				.trip(trip)
				.user(organizer)
				.role(MemberRole.ADMIN)
				.build();
		tripMemberRepository.save(organizerMember);

		return toTripResponse(trip);
	}

	@Transactional(readOnly = true)
	public List<TripResponse> getMyTrips(String userId) {
		return tripRepository.findDistinctByMembersUserIdOrderByCreatedAtDesc(userId).stream()
				.map(this::toTripResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public TripResponse getTripById(String tripId, String requestingUserId) {
		Trip trip = getTripOrThrow(tripId);
		validateMembership(tripId, requestingUserId);
		return toTripResponse(trip);
	}

	@Transactional
	public TripResponse updateTripStatus(String tripId, TripStatus newStatus, String requestingUserId) {
		Trip trip = getTripOrThrow(tripId);
		validateMembership(tripId, requestingUserId);
		validateAdminOrOrganizer(trip, requestingUserId);

		TripStatus currentStatus = trip.getStatus();

		if (currentStatus == TripStatus.PLANNING && newStatus == TripStatus.ACTIVE) {
			validateKittyFundingForActivation(trip);
		} else if (currentStatus == TripStatus.ACTIVE && newStatus == TripStatus.FROZEN) {
			List<Expense> pendingEdits = expenseRepository.findByTripIdAndDeletedFalse(tripId).stream()
					.filter(e -> Boolean.TRUE.equals(e.getEditPending()))
					.toList();
			if (!pendingEdits.isEmpty()) {
				throw new RuntimeException(String.format(
						"Cannot freeze trip. There are %d expense(s) with pending edits. Please approve or reject all pending edits before freezing.",
						pendingEdits.size()));
			}
		} else if (currentStatus == TripStatus.FROZEN && newStatus == TripStatus.SETTLED) {
			throw new RuntimeException(
					"Settlement must be finalised through the settlement endpoint first");
		} else if (currentStatus == newStatus) {
			return toTripResponse(trip);
		} else {
			throw new RuntimeException("Invalid status transition");
		}

		trip.setStatus(newStatus);
		trip = tripRepository.save(trip);

		User actor = userRepository.findById(requestingUserId)
				.orElseThrow(() -> new RuntimeException("User not found"));
		eventPublisher.publishStatusChange(TripStatusEvent.builder()
				.tripId(trip.getId())
				.tripName(trip.getName())
				.oldStatus(currentStatus.name())
				.newStatus(newStatus.name())
				.triggeredByUserId(requestingUserId)
				.triggeredByUserName(actor.getName())
				.timestamp(LocalDateTime.now())
				.build());

		return toTripResponse(trip);
	}

	@Transactional
	public TripResponse inviteMember(String tripId, InviteMemberRequest request, String requestingUserId) {
		Trip trip = getTripOrThrow(tripId);
		validateMembership(tripId, requestingUserId);
		validateAdminOrOrganizer(trip, requestingUserId);

		if (trip.getStatus() != TripStatus.PLANNING) {
			throw new RuntimeException("Members can only be invited while trip is in PLANNING status");
		}

		User targetUser = findUserByPhoneOrEmail(request.getPhone(), request.getEmail())
				.orElseThrow(() -> new RuntimeException("User not found"));

		if (tripMemberRepository.existsByTripIdAndUserId(tripId, targetUser.getId())) {
			throw new RuntimeException("User is already a member");
		}

		int memberCount = tripMemberRepository.countByTripId(tripId);
		Double proratedContribution = calculateProratedContribution(trip);

		TripMember member = TripMember.builder()
				.trip(trip)
				.user(targetUser)
				.role(request.getRole() != null ? request.getRole() : MemberRole.MEMBER)
				.proratedContribution(proratedContribution)
				.build();
		tripMemberRepository.save(member);

		TripResponse response = toTripResponse(trip);
		if (proratedContribution != null) {
			double kittyTarget = trip.getKittyTarget() != null ? trip.getKittyTarget() : 0.0;
			double fullShare = kittyTarget / (memberCount + 1);
			if (proratedContribution < fullShare) {
				response.setLateJoinerWarning(String.format(
						"Late joiner detected. Suggested contribution for %s: ₹%.0f (prorated). Full share would be ₹%.0f.",
						targetUser.getName(),
						proratedContribution,
						fullShare));
			}
		}
		return response;
	}

	@Transactional(readOnly = true)
	public List<MemberResponse> getTripMembers(String tripId, String requestingUserId) {
		validateMembership(tripId, requestingUserId);
		return tripMemberRepository.findByTripId(tripId).stream()
				.map(this::toMemberResponse)
				.toList();
	}

	@Transactional
	public void removeMember(String tripId, String targetUserId, String requestingUserId) {
		Trip trip = getTripOrThrow(tripId);
		validateAdminOrOrganizer(trip, requestingUserId);

		if (trip.getStatus() != TripStatus.PLANNING) {
			throw new RuntimeException("Members can only be removed while trip is in PLANNING status");
		}

		if (trip.getOrganizerId().equals(targetUserId)) {
			throw new RuntimeException("Cannot remove the trip organizer");
		}

		TripMember member = tripMemberRepository.findByTripIdAndUserId(tripId, targetUserId)
				.orElseThrow(() -> new RuntimeException("User is not a member of this trip"));

		tripMemberRepository.delete(member);
	}

	private Trip getTripOrThrow(String tripId) {
		return tripRepository.findById(tripId)
				.orElseThrow(() -> new RuntimeException("Trip not found"));
	}

	private void validateMembership(String tripId, String userId) {
		if (!tripMemberRepository.existsByTripIdAndUserId(tripId, userId)) {
			throw new RuntimeException("Access denied. You are not a member of this trip");
		}
	}

	private void validateAdminOrOrganizer(Trip trip, String userId) {
		if (trip.getOrganizerId().equals(userId)) {
			return;
		}
		TripMember member = tripMemberRepository.findByTripIdAndUserId(trip.getId(), userId)
				.orElseThrow(() -> new RuntimeException("Only trip admin can perform this action"));
		if (member.getRole() != MemberRole.ADMIN) {
			throw new RuntimeException("Only trip admin can perform this action");
		}
	}

	private void validateKittyFundingForActivation(Trip trip) {
		double kittyTarget = trip.getKittyTarget() != null ? trip.getKittyTarget() : 0.0;
		if (kittyTarget == 0.0) {
			return;
		}
		Double actualBalance = paymentRepository.sumKittyDepositsByTripId(trip.getId());
		if (actualBalance == null) {
			actualBalance = 0.0;
		}
		double fundedPercent = (actualBalance / kittyTarget) * 100.0;
		if (fundedPercent < 80.0) {
			throw new RuntimeException(String.format(
					"Kitty must be at least 80%% funded before activating the trip. Current: %.1f%%, Required: 80%%",
					fundedPercent));
		}
	}

	private Double calculateProratedContribution(Trip trip) {
		LocalDate startDate = trip.getStartDate();
		LocalDate endDate = trip.getEndDate();
		LocalDate today = LocalDate.now();

		if (startDate == null || endDate == null || !today.isAfter(startDate)) {
			return null;
		}

		long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
		if (totalDays <= 0) {
			return null;
		}

		long remainingDays = ChronoUnit.DAYS.between(today, endDate) + 1;
		if (remainingDays < 0) {
			remainingDays = 0;
		}

		double kittyTarget = trip.getKittyTarget() != null ? trip.getKittyTarget() : 0.0;
		return kittyTarget * ((double) remainingDays / totalDays);
	}

	private java.util.Optional<User> findUserByPhoneOrEmail(String phone, String email) {
		if (StringUtils.hasText(phone)) {
			java.util.Optional<User> byPhone = userRepository.findByPhone(phone);
			if (byPhone.isPresent()) {
				return byPhone;
			}
		}
		if (StringUtils.hasText(email)) {
			return userRepository.findByEmail(email);
		}
		return java.util.Optional.empty();
	}

	private TripResponse toTripResponse(Trip trip) {
		String organizerName = userRepository.findById(trip.getOrganizerId())
				.map(User::getName)
				.orElse(null);

		double kittyTarget = trip.getKittyTarget() != null ? trip.getKittyTarget() : 0.0;
		double kittyBalance = trip.getKittyBalance() != null ? trip.getKittyBalance() : 0.0;
		double kittyFundedPercent = kittyTarget > 0 ? (kittyBalance / kittyTarget) * 100.0 : 0.0;

		return TripResponse.builder()
				.tripId(trip.getId())
				.name(trip.getName())
				.destination(trip.getDestination())
				.startDate(trip.getStartDate())
				.endDate(trip.getEndDate())
				.status(trip.getStatus())
				.organizerId(trip.getOrganizerId())
				.organizerName(organizerName)
				.kittyTarget(trip.getKittyTarget())
				.kittyBalance(trip.getKittyBalance())
				.kittyFundedPercent(kittyFundedPercent)
				.baseCurrency(trip.getBaseCurrency())
				.memberCount(tripMemberRepository.countByTripId(trip.getId()))
				.createdAt(trip.getCreatedAt())
				.build();
	}

	private MemberResponse toMemberResponse(TripMember member) {
		double contribution = member.getContribution() != null ? member.getContribution() : 0.0;
		double consumption = member.getConsumption() != null ? member.getConsumption() : 0.0;
		double externalPaid = member.getExternalPaid() != null ? member.getExternalPaid() : 0.0;
		double netBalance = contribution + externalPaid - consumption;

		User user = member.getUser();
		return MemberResponse.builder()
				.memberId(member.getId())
				.userId(user.getId())
				.name(user.getName())
				.phone(user.getPhone())
				.role(member.getRole())
				.contribution(contribution)
				.consumption(consumption)
				.externalPaid(externalPaid)
				.netBalance(netBalance)
				.joinedAt(member.getJoinedAt())
				.build();
	}
}
