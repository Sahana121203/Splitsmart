package com.smartexpense.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartexpense.dto.response.FinalSettlementResponse;
import com.smartexpense.dto.response.MemberSettlementResponse;
import com.smartexpense.dto.response.SettlementPreviewResponse;
import com.smartexpense.dto.response.TransferInstructionResponse;
import com.smartexpense.dto.websocket.SettlementEvent;
import com.smartexpense.model.Trip;
import com.smartexpense.model.TripMember;
import com.smartexpense.model.User;
import com.smartexpense.repository.UserRepository;
import com.smartexpense.model.enums.MemberRole;
import com.smartexpense.model.enums.SettlementMode;
import com.smartexpense.model.enums.TripStatus;
import com.smartexpense.repository.TripMemberRepository;
import com.smartexpense.repository.TripRepository;
import com.smartexpense.util.MemberBalance;
import com.smartexpense.util.SettlementAlgorithm;
import com.smartexpense.util.SettlementResult;
import com.smartexpense.util.TransferInstruction;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SettlementService {

	private static final double TOLERANCE = 0.01;

	private final TripRepository tripRepository;
	private final TripMemberRepository tripMemberRepository;
	private final UserRepository userRepository;
	private final TripEventPublisher eventPublisher;

	@Transactional(readOnly = true)
	public SettlementPreviewResponse preview(String tripId, String currentUserId) {
		Trip trip = getTripOrThrow(tripId);
		validateMembership(tripId, currentUserId);
		validatePreviewStatus(trip);

		SettlementResult result = calculateForTrip(tripId);
		return toPreviewResponse(trip, result);
	}

	@Transactional
	public FinalSettlementResponse finalise(String tripId, String currentUserId) {
		Trip trip = getTripOrThrow(tripId);
		validateAdminOrOrganizerForFinalise(trip, currentUserId);

		if (trip.getStatus() != TripStatus.FROZEN) {
			throw new RuntimeException(String.format(
					"Trip must be FROZEN before finalising settlement. Current status: %s",
					trip.getStatus()));
		}

		SettlementResult result = calculateForTrip(tripId);
		trip.setStatus(TripStatus.SETTLED);
		trip = tripRepository.save(trip);

		User actor = userRepository.findById(currentUserId)
				.orElseThrow(() -> new RuntimeException("User not found"));
		eventPublisher.publishSettlementEvent(SettlementEvent.builder()
				.tripId(tripId)
				.eventType("FINALISED")
				.mode(result.getMode())
				.totalTransfers(result.getTotalTransfers())
				.triggeredByUserId(currentUserId)
				.triggeredByUserName(actor.getName())
				.timestamp(LocalDateTime.now())
				.build());

		return toFinalResponse(trip, result, LocalDateTime.now(),
				"Settlement finalised. Trip is now SETTLED.");
	}

	@Transactional(readOnly = true)
	public FinalSettlementResponse getSettlementResult(String tripId, String currentUserId) {
		Trip trip = getTripOrThrow(tripId);
		validateMembership(tripId, currentUserId);

		if (trip.getStatus() != TripStatus.SETTLED) {
			throw new RuntimeException("Settlement not finalised yet");
		}

		SettlementResult result = calculateForTrip(tripId);
		LocalDateTime finalisedAt = trip.getUpdatedAt() != null ? trip.getUpdatedAt() : trip.getCreatedAt();
		return toFinalResponse(trip, result, finalisedAt,
				"Settlement finalised. Trip is now SETTLED.");
	}

	private SettlementResult calculateForTrip(String tripId) {
		List<TripMember> members = tripMemberRepository.findByTripId(tripId);
		List<MemberBalance> balances = members.stream()
				.map(member -> MemberBalance.builder()
						.userId(member.getUser().getId())
						.userName(member.getUser().getName())
						.contribution(member.getContribution())
						.consumption(member.getConsumption())
						.externalPaid(member.getExternalPaid())
						.build())
				.toList();
		return SettlementAlgorithm.calculate(balances);
	}

	private SettlementPreviewResponse toPreviewResponse(Trip trip, SettlementResult result) {
		return SettlementPreviewResponse.builder()
				.tripId(trip.getId())
				.tripName(trip.getName())
				.mode(result.getMode())
				.totalContribution(result.getTotalContribution())
				.totalConsumption(result.getTotalConsumption())
				.surplus(result.getSurplus())
				.totalTransfers(result.getTotalTransfers())
				.memberBalances(mapMemberBalances(result))
				.transferInstructions(mapTransfers(result))
				.canFinalise(trip.getStatus() == TripStatus.FROZEN)
				.statusMessage(buildStatusMessage(result))
				.build();
	}

	private FinalSettlementResponse toFinalResponse(Trip trip, SettlementResult result,
			LocalDateTime finalisedAt, String message) {
		return FinalSettlementResponse.builder()
				.tripId(trip.getId())
				.tripName(trip.getName())
				.mode(result.getMode())
				.transferInstructions(mapTransfers(result))
				.totalContribution(result.getTotalContribution())
				.totalConsumption(result.getTotalConsumption())
				.surplus(result.getSurplus())
				.finalisedAt(finalisedAt)
				.message(message)
				.build();
	}

	private String buildStatusMessage(SettlementResult result) {
		if (result.getTotalTransfers() == 0 && result.getMode() == SettlementMode.BALANCED) {
			return "All members are settled. No transfers needed.";
		}
		if (result.getTotalTransfers() == 0 && result.getMode() == SettlementMode.REFUND) {
			return String.format(
					"Trip is in REFUND mode. ₹%.2f surplus will be returned to all members.",
					result.getSurplus());
		}
		return String.format("%d transfer(s) needed to settle the group.", result.getTotalTransfers());
	}

	private List<MemberSettlementResponse> mapMemberBalances(SettlementResult result) {
		return result.getMemberBalances().stream()
				.map(balance -> MemberSettlementResponse.builder()
						.userId(balance.getUserId())
						.userName(balance.getUserName())
						.contribution(balance.getContribution())
						.consumption(balance.getConsumption())
						.externalPaid(balance.getExternalPaid())
						.finalBalance(balance.getFinalBalance())
						.role(resolveRole(balance.getFinalBalance()))
						.build())
				.toList();
	}

	private List<TransferInstructionResponse> mapTransfers(SettlementResult result) {
		return result.getTransferInstructions().stream()
				.map(this::toTransferResponse)
				.toList();
	}

	private TransferInstructionResponse toTransferResponse(TransferInstruction transfer) {
		return TransferInstructionResponse.builder()
				.fromUserId(transfer.getFromUserId())
				.fromUserName(transfer.getFromUserName())
				.toUserId(transfer.getToUserId())
				.toUserName(transfer.getToUserName())
				.amount(transfer.getAmount())
				.description(String.format("%s pays %s ₹%.2f",
						transfer.getFromUserName(),
						transfer.getToUserName(),
						transfer.getAmount()))
				.build();
	}

	private String resolveRole(Double finalBalance) {
		double balance = finalBalance != null ? finalBalance : 0.0;
		if (balance > TOLERANCE) {
			return "CREDITOR";
		}
		if (balance < -TOLERANCE) {
			return "DEBTOR";
		}
		return "SETTLED";
	}

	private void validatePreviewStatus(Trip trip) {
		if (trip.getStatus() != TripStatus.FROZEN && trip.getStatus() != TripStatus.SETTLED) {
			throw new RuntimeException(
					"Settlement preview is only available when trip is FROZEN or SETTLED");
		}
	}

	private void validateAdminOrOrganizerForFinalise(Trip trip, String userId) {
		if (trip.getOrganizerId().equals(userId)) {
			return;
		}
		TripMember member = tripMemberRepository.findByTripIdAndUserId(trip.getId(), userId)
				.orElseThrow(() -> new RuntimeException("Only trip admin can finalise settlement"));
		if (member.getRole() != MemberRole.ADMIN) {
			throw new RuntimeException("Only trip admin can finalise settlement");
		}
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
}
