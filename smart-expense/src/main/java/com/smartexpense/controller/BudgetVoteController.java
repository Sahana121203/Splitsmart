package com.smartexpense.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartexpense.dto.request.BudgetVoteRequest;
import com.smartexpense.dto.request.CloseBudgetVoteRequest;
import com.smartexpense.service.BudgetVoteService;
import com.smartexpense.util.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/trips/{tripId}/budget-vote")
@RequiredArgsConstructor
public class BudgetVoteController {

	private final BudgetVoteService budgetVoteService;

	@PostMapping
	public ResponseEntity<ApiResponse<?>> submitVote(
			@PathVariable String tripId,
			@RequestBody @Valid BudgetVoteRequest request) {
		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.ok(budgetVoteService.submitVote(tripId, request, userId)));
	}

	@GetMapping("/status")
	public ResponseEntity<ApiResponse<?>> getVoteStatus(@PathVariable String tripId) {
		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		return ResponseEntity.ok(ApiResponse.ok(budgetVoteService.getVoteStatus(tripId, userId)));
	}

	@GetMapping("/result")
	public ResponseEntity<ApiResponse<?>> getResult(@PathVariable String tripId) {
		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		return ResponseEntity.ok(ApiResponse.ok(budgetVoteService.getResult(tripId, userId)));
	}

	@PostMapping("/close")
	public ResponseEntity<ApiResponse<?>> closeVote(
			@PathVariable String tripId,
			@RequestBody CloseBudgetVoteRequest request) {
		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		return ResponseEntity.ok(ApiResponse.ok(budgetVoteService.closeVote(tripId, userId)));
	}
}
