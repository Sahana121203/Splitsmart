package com.smartexpense.controller;

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
import org.springframework.web.bind.annotation.RestController;

import com.smartexpense.dto.request.CreateTripRequest;
import com.smartexpense.dto.request.InviteMemberRequest;
import com.smartexpense.dto.request.UpdateTripStatusRequest;
import com.smartexpense.service.TripService;
import com.smartexpense.util.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {

	private final TripService tripService;

	@PostMapping
	public ResponseEntity<ApiResponse<?>> createTrip(@RequestBody @Valid CreateTripRequest request) {
		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.ok(tripService.createTrip(request, userId)));
	}

	@GetMapping
	public ResponseEntity<ApiResponse<?>> getMyTrips() {
		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		return ResponseEntity.ok(ApiResponse.ok(tripService.getMyTrips(userId)));
	}

	@GetMapping("/{tripId}")
	public ResponseEntity<ApiResponse<?>> getTripById(@PathVariable String tripId) {
		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		return ResponseEntity.ok(ApiResponse.ok(tripService.getTripById(tripId, userId)));
	}

	@PatchMapping("/{tripId}/status")
	public ResponseEntity<ApiResponse<?>> updateTripStatus(
			@PathVariable String tripId,
			@RequestBody @Valid UpdateTripStatusRequest request) {
		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		return ResponseEntity.ok(ApiResponse.ok(
				tripService.updateTripStatus(tripId, request.getNewStatus(), userId)));
	}

	@PostMapping("/{tripId}/invite")
	public ResponseEntity<ApiResponse<?>> inviteMember(
			@PathVariable String tripId,
			@RequestBody @Valid InviteMemberRequest request) {
		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.ok(tripService.inviteMember(tripId, request, userId)));
	}

	@GetMapping("/{tripId}/members")
	public ResponseEntity<ApiResponse<?>> getTripMembers(@PathVariable String tripId) {
		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		return ResponseEntity.ok(ApiResponse.ok(tripService.getTripMembers(tripId, userId)));
	}

	@DeleteMapping("/{tripId}/members/{targetUserId}")
	public ResponseEntity<ApiResponse<?>> removeMember(
			@PathVariable String tripId,
			@PathVariable String targetUserId) {
		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		tripService.removeMember(tripId, targetUserId, userId);
		return ResponseEntity.ok(ApiResponse.ok("Member removed"));
	}
}
