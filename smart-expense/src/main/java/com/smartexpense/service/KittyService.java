package com.smartexpense.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartexpense.dto.request.ExternalPaymentRequest;
import com.smartexpense.dto.request.KittyDepositRequest;
import com.smartexpense.dto.websocket.KittyUpdateEvent;
import com.smartexpense.dto.response.ExternalPaymentResponse;
import com.smartexpense.dto.response.KittyResponse;
import com.smartexpense.dto.response.MemberContributionResponse;
import com.smartexpense.dto.response.PaymentHistoryResponse;
import com.smartexpense.model.Expense;
import com.smartexpense.model.Payment;
import com.smartexpense.model.Trip;
import com.smartexpense.model.TripMember;
import com.smartexpense.model.User;
import com.smartexpense.model.enums.MemberRole;
import com.smartexpense.model.enums.PaymentSource;
import com.smartexpense.model.enums.PaymentType;
import com.smartexpense.model.enums.TripStatus;
import com.smartexpense.repository.ExpenseRepository;
import com.smartexpense.repository.PaymentRepository;
import com.smartexpense.repository.TripMemberRepository;
import com.smartexpense.repository.TripRepository;
import com.smartexpense.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KittyService {

	private final TripRepository tripRepository;
	private final TripMemberRepository tripMemberRepository;
	private final PaymentRepository paymentRepository;
	private final UserRepository userRepository;
	private final ExpenseRepository expenseRepository;
	private final TripEventPublisher eventPublisher;

	@Transactional
	public KittyResponse deposit(String tripId, KittyDepositRequest request, String userId) {
		Trip trip = getTripOrThrow(tripId);
		validateMember(tripId, userId);
		validateDepositAllowed(trip);

		if (request.getAmount() == null || request.getAmount() <= 0) {
			throw new RuntimeException("Deposit amount must be greater than zero");
		}

		Payment payment = Payment.builder()
				.tripId(tripId)
				.userId(userId)
				.amount(request.getAmount())
				.method(request.getMethod())
				.type(PaymentType.KITTY_DEPOSIT)
				.reference(request.getReference())
				.note(request.getNote())
				.build();
		paymentRepository.save(payment);

		TripMember member = tripMemberRepository.findByTripIdAndUserId(tripId, userId)
				.orElseThrow(() -> new RuntimeException("Only trip members can deposit"));
		double currentContribution = member.getContribution() != null ? member.getContribution() : 0.0;
		member.setContribution(currentContribution + request.getAmount());
		tripMemberRepository.save(member);

		double currentBalance = trip.getKittyBalance() != null ? trip.getKittyBalance() : 0.0;
		trip.setKittyBalance(currentBalance + request.getAmount());
		trip = tripRepository.save(trip);

		User depositor = userRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("User not found"));
		double kittyTarget = trip.getKittyTarget() != null ? trip.getKittyTarget() : 0.0;
		double kittyBalance = trip.getKittyBalance() != null ? trip.getKittyBalance() : 0.0;
		double kittyFundedPercent = kittyTarget == 0 ? 100.0 : (kittyBalance / kittyTarget) * 100.0;

		eventPublisher.publishKittyUpdate(KittyUpdateEvent.builder()
				.tripId(tripId)
				.kittyBalance(kittyBalance)
				.kittyFundedPercent(kittyFundedPercent)
				.depositorUserId(userId)
				.depositorName(depositor.getName())
				.depositAmount(request.getAmount())
				.timestamp(LocalDateTime.now())
				.build());

		return buildKittyResponse(trip);
	}

	@Transactional(readOnly = true)
	public KittyResponse getKittyStatus(String tripId, String userId) {
		Trip trip = getTripOrThrow(tripId);
		validateMember(tripId, userId);
		return buildKittyResponse(trip);
	}

	@Transactional(readOnly = true)
	public List<PaymentHistoryResponse> getDepositHistory(String tripId, String userId) {
		Trip trip = getTripOrThrow(tripId);
		validateMember(tripId, userId);

		return paymentRepository.findByTripIdAndTypeOrderByCreatedAtDesc(tripId, PaymentType.KITTY_DEPOSIT).stream()
				.map(this::toPaymentHistoryResponse)
				.toList();
	}

	@Transactional
	public KittyResponse updateKittyTarget(String tripId, Double newTarget, String userId) {
		Trip trip = getTripOrThrow(tripId);
		validateAdminOrOrganizer(trip, userId);

		if (trip.getStatus() != TripStatus.PLANNING) {
			throw new RuntimeException("Kitty target can only be changed during PLANNING status");
		}
		if (newTarget == null || newTarget < 0) {
			throw new RuntimeException("Kitty target cannot be negative");
		}

		trip.setKittyTarget(newTarget);
		trip = tripRepository.save(trip);
		return buildKittyResponse(trip);
	}

	@Transactional
	public ExternalPaymentResponse recordExternalPayment(
			String tripId,
			ExternalPaymentRequest request,
			String currentUserId) {
		Trip trip = getTripOrThrow(tripId);
		validateMember(tripId, currentUserId);

		if (trip.getStatus() != TripStatus.ACTIVE && trip.getStatus() != TripStatus.FROZEN) {
			throw new RuntimeException("External payments can only be recorded when trip is ACTIVE or FROZEN");
		}

		Expense expense = expenseRepository.findById(request.getExpenseId())
				.filter(e -> !Boolean.TRUE.equals(e.getDeleted()))
				.orElseThrow(() -> new RuntimeException("Expense not found"));

		if (!expense.getTrip().getId().equals(tripId)) {
			throw new RuntimeException("Expense not found");
		}

		if (request.getAmount() > expense.getAmount()) {
			throw new RuntimeException("External payment cannot exceed expense amount");
		}

		Payment payment = Payment.builder()
				.tripId(tripId)
				.userId(currentUserId)
				.amount(request.getAmount())
				.method(request.getMethod())
				.type(PaymentType.EXTERNAL_EXPENSE)
				.reference(request.getReference())
				.note(request.getNote())
				.expenseId(request.getExpenseId())
				.build();
		payment = paymentRepository.save(payment);

		TripMember member = tripMemberRepository.findByTripIdAndUserId(tripId, currentUserId)
				.orElseThrow(() -> new RuntimeException("Only trip members can record external payments"));
		double currentExternalPaid = member.getExternalPaid() != null ? member.getExternalPaid() : 0.0;
		member.setExternalPaid(currentExternalPaid + request.getAmount());
		tripMemberRepository.save(member);

		if (expense.getPaidFrom() != PaymentSource.EXTERNAL) {
			expense.setPaidFrom(PaymentSource.EXTERNAL);
			expenseRepository.save(expense);
		}

		return toExternalPaymentResponse(payment, expense);
	}

	@Transactional(readOnly = true)
	public List<ExternalPaymentResponse> getExternalPayments(String tripId, String userId) {
		getTripOrThrow(tripId);
		validateMember(tripId, userId);

		return paymentRepository.findByTripIdAndTypeOrderByCreatedAtDesc(tripId, PaymentType.EXTERNAL_EXPENSE).stream()
				.map(payment -> {
					Expense expense = payment.getExpenseId() != null
							? expenseRepository.findById(payment.getExpenseId()).orElse(null)
							: null;
					return toExternalPaymentResponse(payment, expense);
				})
				.toList();
	}

	private ExternalPaymentResponse toExternalPaymentResponse(Payment payment, Expense expense) {
		String paidByUserName = userRepository.findById(payment.getUserId())
				.map(User::getName)
				.orElse(null);
		String expenseTitle = expense != null ? expense.getTitle() : null;
		String expenseId = payment.getExpenseId() != null ? payment.getExpenseId()
				: (expense != null ? expense.getId() : null);

		return ExternalPaymentResponse.builder()
				.paymentId(payment.getId())
				.tripId(payment.getTripId())
				.expenseId(expenseId)
				.expenseTitle(expenseTitle)
				.paidByUserId(payment.getUserId())
				.paidByUserName(paidByUserName)
				.amount(payment.getAmount())
				.method(payment.getMethod())
				.reference(payment.getReference())
				.note(payment.getNote())
				.createdAt(payment.getCreatedAt())
				.build();
	}

	private KittyResponse buildKittyResponse(Trip trip) {
		String tripId = trip.getId();
		List<TripMember> members = tripMemberRepository.findByTripId(tripId);
		int memberCount = members.size();

		double kittyTarget = trip.getKittyTarget() != null ? trip.getKittyTarget() : 0.0;
		double kittyBalance = trip.getKittyBalance() != null ? trip.getKittyBalance() : 0.0;
		double expectedShare = memberCount > 0 && kittyTarget > 0 ? kittyTarget / memberCount : 0.0;

		List<MemberContributionResponse> memberContributions = new ArrayList<>();
		for (TripMember member : members) {
			User user = member.getUser();
			Double contributedSum = paymentRepository.sumKittyDepositsByTripIdAndUserId(tripId, user.getId());
			double contributed = contributedSum != null ? contributedSum : 0.0;
			double remainingShare = Math.max(0, expectedShare - contributed);

			memberContributions.add(MemberContributionResponse.builder()
					.userId(user.getId())
					.name(user.getName())
					.contributed(contributed)
					.expectedShare(expectedShare)
					.remainingShare(remainingShare)
					.fullyContributed(contributed >= expectedShare)
					.build());
		}

		double kittyFundedPercent = kittyTarget == 0 ? 100.0 : (kittyBalance / kittyTarget) * 100.0;
		boolean readyToActivate = kittyTarget == 0 || kittyFundedPercent >= 80.0;

		LocalDateTime lastDepositAt = paymentRepository
				.findByTripIdAndTypeOrderByCreatedAtDesc(tripId, PaymentType.KITTY_DEPOSIT).stream()
				.findFirst()
				.map(Payment::getCreatedAt)
				.orElse(null);

		return KittyResponse.builder()
				.tripId(tripId)
				.kittyTarget(trip.getKittyTarget())
				.kittyBalance(kittyBalance)
				.kittyFundedPercent(kittyFundedPercent)
				.readyToActivate(readyToActivate)
				.memberContributions(memberContributions)
				.lastDepositAt(lastDepositAt)
				.build();
	}

	private PaymentHistoryResponse toPaymentHistoryResponse(Payment payment) {
		String userName = userRepository.findById(payment.getUserId())
				.map(User::getName)
				.orElse(null);

		return PaymentHistoryResponse.builder()
				.paymentId(payment.getId())
				.userId(payment.getUserId())
				.userName(userName)
				.amount(payment.getAmount())
				.method(payment.getMethod())
				.type(payment.getType())
				.reference(payment.getReference())
				.note(payment.getNote())
				.createdAt(payment.getCreatedAt())
				.build();
	}

	private Trip getTripOrThrow(String tripId) {
		return tripRepository.findById(tripId)
				.orElseThrow(() -> new RuntimeException("Trip not found"));
	}

	private void validateMember(String tripId, String userId) {
		if (!tripMemberRepository.existsByTripIdAndUserId(tripId, userId)) {
			throw new RuntimeException("Only trip members can deposit");
		}
	}

	private void validateDepositAllowed(Trip trip) {
		if (trip.getStatus() == TripStatus.FROZEN || trip.getStatus() == TripStatus.SETTLED) {
			throw new RuntimeException("Deposits not allowed when trip is FROZEN or SETTLED");
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
}
