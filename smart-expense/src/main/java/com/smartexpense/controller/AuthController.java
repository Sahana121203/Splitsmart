package com.smartexpense.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartexpense.dto.request.LoginRequest;
import com.smartexpense.dto.request.RegisterRequest;
import com.smartexpense.service.AuthService;
import com.smartexpense.util.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@PostMapping("/register")
	public ResponseEntity<ApiResponse<?>> register(@RequestBody @Valid RegisterRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.ok(authService.register(request)));
	}

	@PostMapping("/login")
	public ResponseEntity<ApiResponse<?>> login(@RequestBody @Valid LoginRequest request) {
		return ResponseEntity.ok(ApiResponse.ok(authService.login(request)));
	}

	@GetMapping("/me")
	public ResponseEntity<ApiResponse<?>> me(@AuthenticationPrincipal String userId) {
		return ResponseEntity.ok(ApiResponse.ok(authService.toProfileResponse(authService.getCurrentUser(userId))));
	}
}
