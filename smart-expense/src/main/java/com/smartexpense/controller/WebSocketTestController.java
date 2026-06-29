package com.smartexpense.controller;

import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartexpense.dto.websocket.TripStatusEvent;
import com.smartexpense.model.Trip;
import com.smartexpense.repository.TripRepository;
import com.smartexpense.repository.UserRepository;
import com.smartexpense.service.TripEventPublisher;
import com.smartexpense.util.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ws-test")
@RequiredArgsConstructor
public class WebSocketTestController {

	private final TripEventPublisher eventPublisher;
	private final TripRepository tripRepository;
	private final UserRepository userRepository;

	@PostMapping("/broadcast/{tripId}")
	public ResponseEntity<ApiResponse<?>> broadcast(
			@PathVariable String tripId,
			@RequestBody String message) {
		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		Trip trip = tripRepository.findById(tripId)
				.orElseThrow(() -> new RuntimeException("Trip not found"));

		String userName = userRepository.findById(userId)
				.map(u -> u.getName())
				.orElse("Unknown");

		eventPublisher.publishStatusChange(TripStatusEvent.builder()
				.tripId(tripId)
				.tripName(trip.getName())
				.oldStatus("TEST")
				.newStatus(message != null ? message : "TEST")
				.triggeredByUserId(userId)
				.triggeredByUserName(userName)
				.timestamp(LocalDateTime.now())
				.build());

		return ResponseEntity.ok(ApiResponse.ok("Broadcast sent"));
	}
}
