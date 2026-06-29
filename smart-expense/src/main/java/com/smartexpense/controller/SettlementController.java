package com.smartexpense.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartexpense.service.SettlementService;
import com.smartexpense.util.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/trips/{tripId}/settlement")
@RequiredArgsConstructor
public class SettlementController {

	private final SettlementService settlementService;

	@GetMapping("/preview")
	public ResponseEntity<ApiResponse<?>> preview(@PathVariable String tripId) {
		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		return ResponseEntity.ok(ApiResponse.ok(settlementService.preview(tripId, userId)));
	}

	@PostMapping("/finalise")
	public ResponseEntity<ApiResponse<?>> finalise(@PathVariable String tripId) {
		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		return ResponseEntity.ok(ApiResponse.ok(settlementService.finalise(tripId, userId)));
	}

	@GetMapping("/result")
	public ResponseEntity<ApiResponse<?>> getResult(@PathVariable String tripId) {
		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		return ResponseEntity.ok(ApiResponse.ok(settlementService.getSettlementResult(tripId, userId)));
	}
}
