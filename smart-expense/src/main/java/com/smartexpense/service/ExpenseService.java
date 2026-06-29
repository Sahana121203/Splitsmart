package com.smartexpense.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartexpense.dto.request.AddExpenseRequest;
import com.smartexpense.dto.request.EditExpenseRequest;
import com.smartexpense.dto.request.ParticipantShareRequest;
import com.smartexpense.dto.websocket.EditApprovalEvent;
import com.smartexpense.dto.websocket.ExpenseEvent;
import com.smartexpense.dto.response.ExpenseResponse;
import com.smartexpense.dto.response.ExpenseSummaryResponse;
import com.smartexpense.dto.response.ParticipantResponse;
import com.smartexpense.dto.response.PendingEditResponse;
import com.smartexpense.model.Expense;
import com.smartexpense.model.ExpenseParticipant;
import com.smartexpense.model.Trip;
import com.smartexpense.model.TripMember;
import com.smartexpense.model.User;
import com.smartexpense.model.enums.ExpenseCategory;
import com.smartexpense.model.enums.MemberRole;
import com.smartexpense.model.enums.PaymentSource;
import com.smartexpense.model.enums.TripStatus;
import com.smartexpense.repository.ExpenseParticipantRepository;
import com.smartexpense.repository.ExpenseRepository;
import com.smartexpense.repository.TripMemberRepository;
import com.smartexpense.repository.TripRepository;
import com.smartexpense.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExpenseService {

	private final TripRepository tripRepository;
	private final TripMemberRepository tripMemberRepository;
	private final ExpenseRepository expenseRepository;
	private final ExpenseParticipantRepository expenseParticipantRepository;
	private final UserRepository userRepository;
	private final ObjectMapper objectMapper;
	private final TripEventPublisher eventPublisher;

	@Transactional
	public ExpenseResponse addExpense(String tripId, AddExpenseRequest request, String currentUserId) {
		Trip trip = getTripOrThrow(tripId);
		validateMember(tripId, currentUserId);

		if (trip.getStatus() != TripStatus.ACTIVE) {
			throw new RuntimeException("Expenses can only be added when trip is ACTIVE");
		}

		String paidByUserId = StringUtils.hasText(request.getPaidByUserId())
				? request.getPaidByUserId()
				: currentUserId;

		if (!isMember(tripId, paidByUserId)) {
			throw new RuntimeException("Payer must be a trip member");
		}

		for (ParticipantShareRequest participant : request.getParticipants()) {
			if (!isMember(tripId, participant.getUserId())) {
				throw new RuntimeException(
						"Participant " + participant.getUserId() + " is not a member of this trip");
			}
		}

		List<ResolvedShare> resolvedShares = resolveShares(request.getAmount(), request.getParticipants(),
				request.getEqualSplit());

		String currency = StringUtils.hasText(request.getCurrency()) ? request.getCurrency() : "INR";
		String baseCurrency = StringUtils.hasText(trip.getBaseCurrency()) ? trip.getBaseCurrency() : "INR";
		double amountInBase = currency.equals(baseCurrency) ? request.getAmount() : request.getAmount();

		Expense expense = Expense.builder()
				.title(request.getTitle())
				.amount(request.getAmount())
				.currency(currency)
				.amountInBase(amountInBase)
				.category(request.getCategory() != null ? request.getCategory() : ExpenseCategory.OTHER)
				.paidByUserId(paidByUserId)
				.paidFrom(request.getPaidFrom() != null ? request.getPaidFrom() : PaymentSource.KITTY)
				.receiptUrl(request.getReceiptUrl())
				.trip(trip)
				.build();

		for (ResolvedShare resolvedShare : resolvedShares) {
			ExpenseParticipant participant = ExpenseParticipant.builder()
					.userId(resolvedShare.userId())
					.share(resolvedShare.share())
					.expense(expense)
					.build();
			expense.getParticipants().add(participant);

			TripMember member = tripMemberRepository.findByTripIdAndUserId(tripId, resolvedShare.userId())
					.orElseThrow(() -> new RuntimeException("Participant is not a member of this trip"));
			double currentConsumption = member.getConsumption() != null ? member.getConsumption() : 0.0;
			member.setConsumption(currentConsumption + resolvedShare.share());
			tripMemberRepository.save(member);
		}

		expense = expenseRepository.save(expense);

		if (expense.getPaidFrom() == PaymentSource.KITTY) {
			double currentBalance = trip.getKittyBalance() != null ? trip.getKittyBalance() : 0.0;
			trip.setKittyBalance(currentBalance - request.getAmount());
			tripRepository.save(trip);
		} else if (expense.getPaidFrom() == PaymentSource.EXTERNAL) {
			TripMember payer = tripMemberRepository.findByTripIdAndUserId(tripId, paidByUserId)
					.orElseThrow(() -> new RuntimeException("Payer is not a member of this trip"));
			double currentExternalPaid = payer.getExternalPaid() != null ? payer.getExternalPaid() : 0.0;
			payer.setExternalPaid(currentExternalPaid + request.getAmount());
			tripMemberRepository.save(payer);
		}

		User actor = userRepository.findById(currentUserId)
				.orElseThrow(() -> new RuntimeException("User not found"));
		eventPublisher.publishExpenseEvent(ExpenseEvent.builder()
				.tripId(tripId)
				.eventType("ADDED")
				.expenseId(expense.getId())
				.expenseTitle(expense.getTitle())
				.amount(expense.getAmount())
				.triggeredByUserId(currentUserId)
				.triggeredByUserName(actor.getName())
				.timestamp(LocalDateTime.now())
				.build());

		return buildExpenseResponse(expense);
	}

	@Transactional
	public ExpenseResponse requestEdit(String tripId, String expenseId, EditExpenseRequest request,
			String currentUserId) {
		Trip trip = getTripOrThrow(tripId);
		validateMember(tripId, currentUserId);
		Expense expense = getActiveExpense(tripId, expenseId);

		if (trip.getStatus() != TripStatus.ACTIVE) {
			throw new RuntimeException("Expenses can only be edited when trip is ACTIVE");
		}
		if (Boolean.TRUE.equals(expense.getEditPending())) {
			throw new RuntimeException(
					"This expense already has a pending edit. Wait for admin to approve or reject it first.");
		}

		if (isAdminOrOrganizer(trip, currentUserId)) {
			applyEdit(trip, expense, request);
			expense = expenseRepository.save(expense);

			User actor = userRepository.findById(currentUserId)
					.orElseThrow(() -> new RuntimeException("User not found"));
			eventPublisher.publishExpenseEvent(ExpenseEvent.builder()
					.tripId(tripId)
					.eventType("EDITED")
					.expenseId(expense.getId())
					.expenseTitle(expense.getTitle())
					.amount(expense.getAmount())
					.triggeredByUserId(currentUserId)
					.triggeredByUserName(actor.getName())
					.timestamp(LocalDateTime.now())
					.build());

			return buildExpenseResponse(expense);
		}

		try {
			expense.setEditPending(true);
			expense.setEditRequestedBy(currentUserId);
			expense.setPendingEditJson(objectMapper.writeValueAsString(request));
			expense = expenseRepository.save(expense);
		} catch (JsonProcessingException ex) {
			throw new RuntimeException("Failed to serialize edit request");
		}

		String requesterName = userRepository.findById(currentUserId)
				.map(User::getName)
				.orElse("Unknown");
		eventPublisher.publishEditApprovalEvent(EditApprovalEvent.builder()
				.tripId(tripId)
				.expenseId(expense.getId())
				.expenseTitle(expense.getTitle())
				.eventType("EDIT_REQUESTED")
				.requestedByUserId(currentUserId)
				.requestedByUserName(requesterName)
				.adminUserId(null)
				.adminUserName(null)
				.timestamp(LocalDateTime.now())
				.build());

		return buildExpenseResponse(expense);
	}

	@Transactional
	public ExpenseResponse approveEdit(String tripId, String expenseId, String currentUserId) {
		Trip trip = getTripOrThrow(tripId);
		if (!isAdminOrOrganizer(trip, currentUserId)) {
			throw new RuntimeException("Only admin can approve edits");
		}

		Expense expense = getActiveExpense(tripId, expenseId);
		if (!Boolean.TRUE.equals(expense.getEditPending())) {
			throw new RuntimeException("No pending edit found for this expense");
		}

		EditExpenseRequest editRequest = deserializeEditRequest(expense.getPendingEditJson());
		applyEdit(trip, expense, editRequest);

		String requestedByUserId = expense.getEditRequestedBy();
		String requestedByUserName = requestedByUserId != null
				? userRepository.findById(requestedByUserId).map(User::getName).orElse("")
				: "";
		String adminName = userRepository.findById(currentUserId)
				.map(User::getName)
				.orElse("Unknown");

		expense.setEditPending(false);
		expense.setEditRequestedBy(null);
		expense.setPendingEditJson(null);
		expense = expenseRepository.save(expense);

		eventPublisher.publishEditApprovalEvent(EditApprovalEvent.builder()
				.tripId(tripId)
				.expenseId(expense.getId())
				.expenseTitle(expense.getTitle())
				.eventType("EDIT_APPROVED")
				.requestedByUserId(requestedByUserId)
				.requestedByUserName(requestedByUserName)
				.adminUserId(currentUserId)
				.adminUserName(adminName)
				.timestamp(LocalDateTime.now())
				.build());

		eventPublisher.publishExpenseEvent(ExpenseEvent.builder()
				.tripId(tripId)
				.eventType("EDIT_APPROVED")
				.expenseId(expense.getId())
				.expenseTitle(expense.getTitle())
				.amount(expense.getAmount())
				.triggeredByUserId(currentUserId)
				.triggeredByUserName(adminName)
				.timestamp(LocalDateTime.now())
				.build());

		return buildExpenseResponse(expense);
	}

	@Transactional
	public ExpenseResponse rejectEdit(String tripId, String expenseId, String currentUserId) {
		Trip trip = getTripOrThrow(tripId);
		if (!isAdminOrOrganizer(trip, currentUserId)) {
			throw new RuntimeException("Only admin can reject edits");
		}

		Expense expense = getActiveExpense(tripId, expenseId);
		if (!Boolean.TRUE.equals(expense.getEditPending())) {
			throw new RuntimeException("No pending edit found for this expense");
		}

		String requestedByUserId = expense.getEditRequestedBy();
		String requestedByUserName = requestedByUserId != null
				? userRepository.findById(requestedByUserId).map(User::getName).orElse("")
				: "";
		String adminName = userRepository.findById(currentUserId)
				.map(User::getName)
				.orElse("Unknown");

		expense.setEditPending(false);
		expense.setEditRequestedBy(null);
		expense.setPendingEditJson(null);
		expense = expenseRepository.save(expense);

		eventPublisher.publishEditApprovalEvent(EditApprovalEvent.builder()
				.tripId(tripId)
				.expenseId(expense.getId())
				.expenseTitle(expense.getTitle())
				.eventType("EDIT_REJECTED")
				.requestedByUserId(requestedByUserId)
				.requestedByUserName(requestedByUserName)
				.adminUserId(currentUserId)
				.adminUserName(adminName)
				.timestamp(LocalDateTime.now())
				.build());

		return buildExpenseResponse(expense);
	}

	@Transactional(readOnly = true)
	public List<PendingEditResponse> getPendingEdits(String tripId, String currentUserId) {
		Trip trip = getTripOrThrow(tripId);
		if (!isAdminOrOrganizer(trip, currentUserId)) {
			throw new RuntimeException("Only admin can view pending edits");
		}

		return expenseRepository.findByTripIdAndDeletedFalse(tripId).stream()
				.filter(e -> Boolean.TRUE.equals(e.getEditPending()))
				.map(this::toPendingEditResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public ExpenseSummaryResponse getExpenses(
			String tripId,
			String currentUserId,
			ExpenseCategory categoryFilter,
			String paidByFilter,
			LocalDate dateFrom,
			LocalDate dateTo) {
		getTripOrThrow(tripId);
		validateMember(tripId, currentUserId);

		List<Expense> expenses = fetchFilteredExpenses(tripId, categoryFilter, paidByFilter, dateFrom, dateTo);

		double totalAmount = expenses.stream()
				.mapToDouble(e -> e.getAmount() != null ? e.getAmount() : 0.0)
				.sum();

		Double myTotalShare = expenseRepository.sumConsumptionByTripIdAndUserId(tripId, currentUserId);
		if (myTotalShare == null) {
			myTotalShare = 0.0;
		}

		Map<String, Double> categoryBreakdown = new LinkedHashMap<>();
		for (Expense expense : expenses) {
			String categoryName = expense.getCategory().name();
			categoryBreakdown.merge(categoryName, expense.getAmount(), Double::sum);
		}

		Map<String, Double> memberSpendBreakdown = new LinkedHashMap<>();
		for (Expense expense : expenses) {
			String payerName = userRepository.findById(expense.getPaidByUserId())
					.map(User::getName)
					.orElse(expense.getPaidByUserId());
			memberSpendBreakdown.merge(payerName, expense.getAmount(), Double::sum);
		}

		List<ExpenseResponse> expenseResponses = expenses.stream()
				.map(this::buildExpenseResponse)
				.toList();

		return ExpenseSummaryResponse.builder()
				.tripId(tripId)
				.totalExpenses(expenses.size())
				.totalAmount(totalAmount)
				.myTotalShare(myTotalShare)
				.categoryBreakdown(categoryBreakdown)
				.memberSpendBreakdown(memberSpendBreakdown)
				.expenses(expenseResponses)
				.build();
	}

	@Transactional(readOnly = true)
	public ExpenseResponse getExpenseById(String tripId, String expenseId, String currentUserId) {
		getTripOrThrow(tripId);
		validateMember(tripId, currentUserId);
		return buildExpenseResponse(getActiveExpense(tripId, expenseId));
	}

	@Transactional
	public void softDeleteExpense(String tripId, String expenseId, String currentUserId) {
		Trip trip = getTripOrThrow(tripId);
		if (!isMember(tripId, currentUserId)) {
			throw new RuntimeException("Access denied. You are not a member of this trip");
		}

		if (!isAdminOrOrganizer(trip, currentUserId)) {
			throw new RuntimeException("Only admin can delete expenses");
		}

		if (trip.getStatus() == TripStatus.SETTLED) {
			throw new RuntimeException("Cannot delete expense in SETTLED trip");
		}

		Expense expense = getActiveExpense(tripId, expenseId);

		expense.setDeleted(true);
		expenseRepository.save(expense);

		List<ExpenseParticipant> participants = expenseParticipantRepository.findByExpenseId(expenseId);
		for (ExpenseParticipant participant : participants) {
			TripMember member = tripMemberRepository.findByTripIdAndUserId(tripId, participant.getUserId())
					.orElseThrow(() -> new RuntimeException("Participant is not a member of this trip"));
			double currentConsumption = member.getConsumption() != null ? member.getConsumption() : 0.0;
			member.setConsumption(currentConsumption - participant.getShare());
			tripMemberRepository.save(member);
		}

		if (expense.getPaidFrom() == PaymentSource.KITTY) {
			double currentBalance = trip.getKittyBalance() != null ? trip.getKittyBalance() : 0.0;
			trip.setKittyBalance(currentBalance + expense.getAmount());
			tripRepository.save(trip);
		} else if (expense.getPaidFrom() == PaymentSource.EXTERNAL) {
			TripMember payer = tripMemberRepository.findByTripIdAndUserId(tripId, expense.getPaidByUserId())
					.orElseThrow(() -> new RuntimeException("Payer is not a member of this trip"));
			double currentExternalPaid = payer.getExternalPaid() != null ? payer.getExternalPaid() : 0.0;
			payer.setExternalPaid(currentExternalPaid - expense.getAmount());
			tripMemberRepository.save(payer);
		}

		String actorName = userRepository.findById(currentUserId)
				.map(User::getName)
				.orElse("Unknown");
		eventPublisher.publishExpenseEvent(ExpenseEvent.builder()
				.tripId(tripId)
				.eventType("DELETED")
				.expenseId(expense.getId())
				.expenseTitle(expense.getTitle())
				.amount(expense.getAmount())
				.triggeredByUserId(currentUserId)
				.triggeredByUserName(actorName)
				.timestamp(LocalDateTime.now())
				.build());
	}

	private void applyEdit(Trip trip, Expense expense, EditExpenseRequest request) {
		String tripId = trip.getId();

		if (request.getParticipants() != null) {
			List<ExpenseParticipant> existingParticipants =
					expenseParticipantRepository.findByExpenseId(expense.getId());
			for (ExpenseParticipant participant : existingParticipants) {
				TripMember member = tripMemberRepository.findByTripIdAndUserId(tripId, participant.getUserId())
						.orElseThrow(() -> new RuntimeException("Participant is not a member of this trip"));
				double currentConsumption = member.getConsumption() != null ? member.getConsumption() : 0.0;
				member.setConsumption(currentConsumption - participant.getShare());
				tripMemberRepository.save(member);
			}

			if (expense.getPaidFrom() == PaymentSource.KITTY) {
				double currentBalance = trip.getKittyBalance() != null ? trip.getKittyBalance() : 0.0;
				trip.setKittyBalance(currentBalance + expense.getAmount());
				tripRepository.save(trip);
			}

			expenseParticipantRepository.deleteAll(existingParticipants);
			expense.getParticipants().clear();
		}

		if (request.getTitle() != null) {
			expense.setTitle(request.getTitle());
		}
		if (request.getCategory() != null) {
			expense.setCategory(request.getCategory());
		}
		if (request.getPaidByUserId() != null) {
			if (!isMember(tripId, request.getPaidByUserId())) {
				throw new RuntimeException("Payer must be a trip member");
			}
			expense.setPaidByUserId(request.getPaidByUserId());
		}
		if (request.getPaidFrom() != null) {
			expense.setPaidFrom(request.getPaidFrom());
		}
		if (request.getReceiptUrl() != null) {
			expense.setReceiptUrl(request.getReceiptUrl());
		}
		if (request.getAmount() != null) {
			expense.setAmount(request.getAmount());
			expense.setAmountInBase(request.getAmount());
		}

		if (request.getParticipants() != null) {
			for (ParticipantShareRequest participant : request.getParticipants()) {
				if (!isMember(tripId, participant.getUserId())) {
					throw new RuntimeException(
							"Participant " + participant.getUserId() + " is not a member of this trip");
				}
			}

			List<ResolvedShare> resolvedShares = resolveShares(
					expense.getAmount(), request.getParticipants(), request.getEqualSplit());

			expense.getParticipants().clear();
			for (ResolvedShare resolvedShare : resolvedShares) {
				ExpenseParticipant participant = ExpenseParticipant.builder()
						.userId(resolvedShare.userId())
						.share(resolvedShare.share())
						.expense(expense)
						.build();
				expense.getParticipants().add(participant);

				TripMember member = tripMemberRepository.findByTripIdAndUserId(tripId, resolvedShare.userId())
						.orElseThrow(() -> new RuntimeException("Participant is not a member of this trip"));
				double currentConsumption = member.getConsumption() != null ? member.getConsumption() : 0.0;
				member.setConsumption(currentConsumption + resolvedShare.share());
				tripMemberRepository.save(member);
			}

			if (expense.getPaidFrom() == PaymentSource.KITTY) {
				double currentBalance = trip.getKittyBalance() != null ? trip.getKittyBalance() : 0.0;
				trip.setKittyBalance(currentBalance - expense.getAmount());
				tripRepository.save(trip);
			}
		}
	}

	private EditExpenseRequest deserializeEditRequest(String json) {
		try {
			return objectMapper.readerFor(EditExpenseRequest.class)
					.without(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
					.readValue(json);
		} catch (JsonProcessingException ex) {
			throw new RuntimeException("Failed to deserialize pending edit");
		}
	}

	private PendingEditResponse toPendingEditResponse(Expense expense) {
		String requestedByName = userRepository.findById(expense.getEditRequestedBy())
				.map(User::getName)
				.orElse(null);

		return PendingEditResponse.builder()
				.expenseId(expense.getId())
				.expenseTitle(expense.getTitle())
				.currentAmount(expense.getAmount())
				.currentCategory(expense.getCategory())
				.requestedByUserId(expense.getEditRequestedBy())
				.requestedByUserName(requestedByName)
				.proposedChangesJson(expense.getPendingEditJson())
				.requestedAt(expense.getEditedAt())
				.build();
	}

	private Expense getActiveExpense(String tripId, String expenseId) {
		Expense expense = expenseRepository.findById(expenseId)
				.filter(e -> !Boolean.TRUE.equals(e.getDeleted()))
				.orElseThrow(() -> new RuntimeException("Expense not found"));

		if (!expense.getTrip().getId().equals(tripId)) {
			throw new RuntimeException("Expense not found");
		}
		return expense;
	}

	private List<Expense> fetchFilteredExpenses(
			String tripId,
			ExpenseCategory categoryFilter,
			String paidByFilter,
			LocalDate dateFrom,
			LocalDate dateTo) {
		if (categoryFilter != null) {
			return expenseRepository.findByTripIdAndDeletedFalseAndCategory(tripId, categoryFilter);
		}
		if (StringUtils.hasText(paidByFilter)) {
			return expenseRepository.findByTripIdAndDeletedFalseAndPaidByUserId(tripId, paidByFilter);
		}
		if (dateFrom != null || dateTo != null) {
			LocalDate from = dateFrom != null ? dateFrom : LocalDate.of(1970, 1, 1);
			LocalDate to = dateTo != null ? dateTo : LocalDate.now();
			LocalDateTime start = from.atStartOfDay();
			LocalDateTime end = to.atTime(23, 59, 59);
			return expenseRepository.findByTripIdAndDateRange(tripId, start, end);
		}
		return expenseRepository.findByTripIdAndDeletedFalse(tripId);
	}

	private List<ResolvedShare> resolveShares(
			Double amount,
			List<ParticipantShareRequest> participants,
			Boolean equalSplit) {
		boolean useEqualSplit = equalSplit == null || Boolean.TRUE.equals(equalSplit);
		List<ResolvedShare> resolved = new ArrayList<>();

		if (useEqualSplit) {
			double sharePerPerson = amount / participants.size();
			for (ParticipantShareRequest participant : participants) {
				resolved.add(new ResolvedShare(participant.getUserId(), sharePerPerson));
			}
		} else {
			double sharesSum = participants.stream()
					.mapToDouble(p -> p.getShare() != null ? p.getShare() : 0.0)
					.sum();
			if (Math.abs(sharesSum - amount) > 0.01) {
				throw new RuntimeException(String.format(
						"Participant shares must sum to the total amount. Expected: %s, Got: %s",
						amount, sharesSum));
			}
			for (ParticipantShareRequest participant : participants) {
				resolved.add(new ResolvedShare(participant.getUserId(), participant.getShare()));
			}
		}
		return resolved;
	}

	private List<ResolvedShare> resolveShares(AddExpenseRequest request) {
		return resolveShares(request.getAmount(), request.getParticipants(), request.getEqualSplit());
	}

	private ExpenseResponse buildExpenseResponse(Expense expense) {
		String paidByUserName = userRepository.findById(expense.getPaidByUserId())
				.map(User::getName)
				.orElse(null);

		List<ExpenseParticipant> participants = expenseParticipantRepository.findByExpenseId(expense.getId());
		List<ParticipantResponse> participantResponses = participants.stream()
				.map(p -> ParticipantResponse.builder()
						.userId(p.getUserId())
						.userName(userRepository.findById(p.getUserId()).map(User::getName).orElse(null))
						.share(p.getShare())
						.build())
				.toList();

		double totalParticipantShares = participants.stream()
				.mapToDouble(p -> p.getShare() != null ? p.getShare() : 0.0)
				.sum();

		return ExpenseResponse.builder()
				.expenseId(expense.getId())
				.tripId(expense.getTrip().getId())
				.title(expense.getTitle())
				.amount(expense.getAmount())
				.currency(expense.getCurrency())
				.amountInBase(expense.getAmountInBase())
				.category(expense.getCategory())
				.paidByUserId(expense.getPaidByUserId())
				.paidByUserName(paidByUserName)
				.paidFrom(expense.getPaidFrom())
				.receiptUrl(expense.getReceiptUrl())
				.editPending(expense.getEditPending())
				.deleted(expense.getDeleted())
				.participants(participantResponses)
				.totalParticipantShares(totalParticipantShares)
				.createdAt(expense.getCreatedAt())
				.editedAt(expense.getEditedAt())
				.build();
	}

	private Trip getTripOrThrow(String tripId) {
		return tripRepository.findById(tripId)
				.orElseThrow(() -> new RuntimeException("Trip not found"));
	}

	private void validateMember(String tripId, String userId) {
		if (!isMember(tripId, userId)) {
			throw new RuntimeException("Only trip members can add expenses");
		}
	}

	private boolean isMember(String tripId, String userId) {
		return tripMemberRepository.existsByTripIdAndUserId(tripId, userId);
	}

	private boolean isAdminOrOrganizer(Trip trip, String userId) {
		if (trip.getOrganizerId().equals(userId)) {
			return true;
		}
		return tripMemberRepository.findByTripIdAndUserId(trip.getId(), userId)
				.map(member -> member.getRole() == MemberRole.ADMIN)
				.orElse(false);
	}

	private record ResolvedShare(String userId, Double share) {
	}
}
