package com.smartexpense.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartexpense.dto.request.BudgetVoteRequest;
import com.smartexpense.dto.response.BudgetResultResponse;
import com.smartexpense.dto.response.BudgetVoteStatusResponse;
import com.smartexpense.model.BudgetVote;
import com.smartexpense.model.Trip;
import com.smartexpense.model.TripMember;
import com.smartexpense.model.User;
import com.smartexpense.model.enums.MemberRole;
import com.smartexpense.model.enums.TripStatus;
import com.smartexpense.repository.BudgetVoteRepository;
import com.smartexpense.repository.TripMemberRepository;
import com.smartexpense.repository.TripRepository;
import com.smartexpense.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetVoteService {

	private final TripRepository tripRepository;
	private final TripMemberRepository tripMemberRepository;
	private final BudgetVoteRepository budgetVoteRepository;
	private final UserRepository userRepository;

	@Transactional
	public BudgetVoteStatusResponse submitVote(String tripId, BudgetVoteRequest request, String userId) {
		Trip trip = getTripOrThrow(tripId);
		validateMember(tripId, userId);

		if (trip.getStatus() != TripStatus.PLANNING) {
			throw new RuntimeException("Budget voting is only allowed during PLANNING status");
		}
		if (Boolean.TRUE.equals(trip.getBudgetVoteClosed())) {
			throw new RuntimeException("Budget vote has been closed");
		}
		if (budgetVoteRepository.existsByTripIdAndUserId(tripId, userId)) {
			throw new RuntimeException("You have already submitted your budget vote");
		}

		User user = userRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("User not found"));

		BudgetVote vote = BudgetVote.builder()
				.trip(trip)
				.user(user)
				.maxBudget(request.getMaxBudget())
				.build();
		budgetVoteRepository.save(vote);

		int memberCount = tripMemberRepository.countByTripId(tripId);
		int voteCount = budgetVoteRepository.countByTripId(tripId);
		int quorumRequired = calculateQuorumRequired(memberCount, trip.getBudgetVoteQuorumPercent());

		if (voteCount >= quorumRequired) {
			log.info("Quorum reached for trip: {}", tripId);
		}

		return buildVoteStatus(trip, userId);
	}

	@Transactional(readOnly = true)
	public BudgetVoteStatusResponse getVoteStatus(String tripId, String userId) {
		Trip trip = getTripOrThrow(tripId);
		validateMember(tripId, userId);
		return buildVoteStatus(trip, userId);
	}

	@Transactional(readOnly = true)
	public BudgetResultResponse getResult(String tripId, String userId) {
		Trip trip = getTripOrThrow(tripId);
		validateMember(tripId, userId);

		int memberCount = tripMemberRepository.countByTripId(tripId);
		int voteCount = budgetVoteRepository.countByTripId(tripId);
		int quorumRequired = calculateQuorumRequired(memberCount, trip.getBudgetVoteQuorumPercent());

		if (!isResultAccessible(trip, voteCount, memberCount)) {
			throw new RuntimeException(String.format(
					"Result not available yet. Waiting for more members to vote. Current votes: %d/%d",
					voteCount, quorumRequired));
		}

		if (voteCount == 0) {
			throw new RuntimeException("No votes submitted yet");
		}

		List<Double> sortedBudgets = budgetVoteRepository.findAllBudgetsSorted(tripId);
		double median = calculateMedian(sortedBudgets);
		Double rangeMin = budgetVoteRepository.findMinBudget(tripId);
		Double rangeMax = budgetVoteRepository.findMaxBudget(tripId);
		Double avgBudget = budgetVoteRepository.findAvgBudget(tripId);

		long roundedMin = roundToNearest500(rangeMin);
		long roundedMax = roundToNearest500(rangeMax);
		String suggestion = String.format(
				"Your group is comfortable with ₹%,d – ₹%,d", roundedMin, roundedMax);

		return BudgetResultResponse.builder()
				.medianBudget(median)
				.rangeMin(rangeMin)
				.rangeMax(rangeMax)
				.avgBudget(avgBudget)
				.voteCount(voteCount)
				.suggestion(suggestion)
				.build();
	}

	@Transactional
	public BudgetVoteStatusResponse closeVote(String tripId, String requestingUserId) {
		Trip trip = getTripOrThrow(tripId);
		validateAdminOrOrganizer(trip, requestingUserId);

		if (trip.getStatus() != TripStatus.PLANNING) {
			throw new RuntimeException("Budget voting is only allowed during PLANNING status");
		}
		if (Boolean.TRUE.equals(trip.getBudgetVoteClosed())) {
			throw new RuntimeException("Budget vote has already been closed");
		}

		trip.setBudgetVoteClosed(true);
		tripRepository.save(trip);

		return buildVoteStatus(trip, requestingUserId);
	}

	private Trip getTripOrThrow(String tripId) {
		return tripRepository.findById(tripId)
				.orElseThrow(() -> new RuntimeException("Trip not found"));
	}

	private void validateMember(String tripId, String userId) {
		if (!tripMemberRepository.existsByTripIdAndUserId(tripId, userId)) {
			throw new RuntimeException("Only trip members can vote");
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

	private BudgetVoteStatusResponse buildVoteStatus(Trip trip, String userId) {
		String tripId = trip.getId();
		int memberCount = tripMemberRepository.countByTripId(tripId);
		int voteCount = budgetVoteRepository.countByTripId(tripId);
		int quorumRequired = calculateQuorumRequired(memberCount, trip.getBudgetVoteQuorumPercent());
		boolean quorumReached = voteCount >= quorumRequired;
		boolean voteClosed = Boolean.TRUE.equals(trip.getBudgetVoteClosed());
		boolean hasCurrentUserVoted = budgetVoteRepository.existsByTripIdAndUserId(tripId, userId);

		String message;
		if (voteClosed) {
			message = "Vote has been closed by the organiser.";
		} else if (quorumReached) {
			message = "Quorum reached. Result is available.";
		} else {
			message = String.format("Waiting for %d more members to vote", quorumRequired - voteCount);
		}

		return BudgetVoteStatusResponse.builder()
				.hasCurrentUserVoted(hasCurrentUserVoted)
				.voteCount(voteCount)
				.memberCount(memberCount)
				.quorumRequired(quorumRequired)
				.quorumReached(quorumReached)
				.voteClosed(voteClosed)
				.deadline(trip.getBudgetVoteDeadline())
				.message(message)
				.build();
	}

	private int calculateQuorumRequired(int memberCount, Integer quorumPercent) {
		int percent = quorumPercent != null ? quorumPercent : 50;
		return (int) Math.ceil(memberCount * percent / 100.0);
	}

	private boolean isResultAccessible(Trip trip, int voteCount, int memberCount) {
		if (Boolean.TRUE.equals(trip.getBudgetVoteClosed())) {
			return true;
		}
		int quorumRequired = calculateQuorumRequired(memberCount, trip.getBudgetVoteQuorumPercent());
		if (voteCount >= quorumRequired) {
			return true;
		}
		return trip.getBudgetVoteDeadline() != null
				&& LocalDateTime.now().isAfter(trip.getBudgetVoteDeadline());
	}

	private double calculateMedian(List<Double> sorted) {
		int mid = sorted.size() / 2;
		if (sorted.size() % 2 == 1) {
			return sorted.get(mid);
		}
		return (sorted.get(mid - 1) + sorted.get(mid)) / 2.0;
	}

	private long roundToNearest500(double value) {
		return Math.round(value / 500.0) * 500;
	}
}
