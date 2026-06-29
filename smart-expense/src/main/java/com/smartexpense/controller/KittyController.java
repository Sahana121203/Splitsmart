package com.smartexpense.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartexpense.dto.request.ExternalPaymentRequest;
import com.smartexpense.dto.request.KittyDepositRequest;
import com.smartexpense.dto.request.KittyTargetUpdateRequest;
import com.smartexpense.service.KittyService;
import com.smartexpense.util.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/trips/{tripId}/kitty")
@RequiredArgsConstructor
public class KittyController {

	private final KittyService kittyService;

	@GetMapping
	public ResponseEntity<ApiResponse<?>> getKittyStatus(@PathVariable String tripId) {
		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		return ResponseEntity.ok(ApiResponse.ok(kittyService.getKittyStatus(tripId, userId)));
	}

	@PostMapping("/deposit")
	public ResponseEntity<ApiResponse<?>> deposit(
			@PathVariable String tripId,
			@RequestBody @Valid KittyDepositRequest request) {
		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.ok(kittyService.deposit(tripId, request, userId)));
	}

	@GetMapping("/history")
	public ResponseEntity<ApiResponse<?>> getDepositHistory(@PathVariable String tripId) {
		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		return ResponseEntity.ok(ApiResponse.ok(kittyService.getDepositHistory(tripId, userId)));
	}

	@PatchMapping("/target")
	public ResponseEntity<ApiResponse<?>> updateKittyTarget(
			@PathVariable String tripId,
			@RequestBody @Valid KittyTargetUpdateRequest request) {
		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		return ResponseEntity.ok(ApiResponse.ok(
				kittyService.updateKittyTarget(tripId, request.getNewTarget(), userId)));
	}

	@PostMapping("/external-payment")
	public ResponseEntity<ApiResponse<?>> recordExternalPayment(
			@PathVariable String tripId,
			@RequestBody @Valid ExternalPaymentRequest request) {
		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.ok(kittyService.recordExternalPayment(tripId, request, userId)));
	}

	@GetMapping("/external-payments")
	public ResponseEntity<ApiResponse<?>> getExternalPayments(@PathVariable String tripId) {
		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		return ResponseEntity.ok(ApiResponse.ok(kittyService.getExternalPayments(tripId, userId)));
	}
}
