package com.smartexpense.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartexpense.dto.request.AddExpenseRequest;
import com.smartexpense.dto.request.EditExpenseRequest;
import com.smartexpense.model.enums.ExpenseCategory;
import com.smartexpense.service.ExpenseService;
import com.smartexpense.util.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/trips/{tripId}/expenses")
@RequiredArgsConstructor
public class ExpenseController {

	private final ExpenseService expenseService;

	@PostMapping
	public ResponseEntity<ApiResponse<?>> addExpense(
			@PathVariable String tripId,
			@RequestBody @Valid AddExpenseRequest request) {
		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.ok(expenseService.addExpense(tripId, request, userId)));
	}

	@GetMapping("/pending-edits")
	public ResponseEntity<ApiResponse<?>> getPendingEdits(@PathVariable String tripId) {
		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		return ResponseEntity.ok(ApiResponse.ok(expenseService.getPendingEdits(tripId, userId)));
	}

	@GetMapping
	public ResponseEntity<ApiResponse<?>> getExpenses(
			@PathVariable String tripId,
			@RequestParam(required = false) ExpenseCategory category,
			@RequestParam(required = false) String paidBy,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		return ResponseEntity.ok(ApiResponse.ok(
				expenseService.getExpenses(tripId, userId, category, paidBy, dateFrom, dateTo)));
	}

	@GetMapping("/{expenseId}")
	public ResponseEntity<ApiResponse<?>> getExpenseById(
			@PathVariable String tripId,
			@PathVariable String expenseId) {
		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		return ResponseEntity.ok(ApiResponse.ok(
				expenseService.getExpenseById(tripId, expenseId, userId)));
	}

	@PatchMapping("/{expenseId}")
	public ResponseEntity<ApiResponse<?>> requestEdit(
			@PathVariable String tripId,
			@PathVariable String expenseId,
			@RequestBody @Valid EditExpenseRequest request) {
		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		return ResponseEntity.ok(ApiResponse.ok(
				expenseService.requestEdit(tripId, expenseId, request, userId)));
	}

	@PostMapping("/{expenseId}/approve")
	public ResponseEntity<ApiResponse<?>> approveEdit(
			@PathVariable String tripId,
			@PathVariable String expenseId) {
		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		return ResponseEntity.ok(ApiResponse.ok(
				expenseService.approveEdit(tripId, expenseId, userId)));
	}

	@PostMapping("/{expenseId}/reject")
	public ResponseEntity<ApiResponse<?>> rejectEdit(
			@PathVariable String tripId,
			@PathVariable String expenseId) {
		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		return ResponseEntity.ok(ApiResponse.ok(
				expenseService.rejectEdit(tripId, expenseId, userId)));
	}

	@DeleteMapping("/{expenseId}")
	public ResponseEntity<ApiResponse<?>> softDeleteExpense(
			@PathVariable String tripId,
			@PathVariable String expenseId) {
		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		expenseService.softDeleteExpense(tripId, expenseId, userId);
		return ResponseEntity.ok(ApiResponse.ok("Expense deleted"));
	}
}
