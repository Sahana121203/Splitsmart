package com.smartexpense.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.smartexpense.dto.request.LoginRequest;
import com.smartexpense.dto.request.RegisterRequest;
import com.smartexpense.dto.response.AuthResponse;
import com.smartexpense.dto.response.UserProfileResponse;
import com.smartexpense.model.User;
import com.smartexpense.repository.UserRepository;
import com.smartexpense.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;

	public AuthResponse register(RegisterRequest request) {
		if (userRepository.existsByPhone(request.getPhone())) {
			throw new RuntimeException("Phone already registered");
		}
		if (StringUtils.hasText(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
			throw new RuntimeException("Email already registered");
		}

		User user = User.builder()
				.name(request.getName())
				.phone(request.getPhone())
				.email(StringUtils.hasText(request.getEmail()) ? request.getEmail() : null)
				.passwordHash(passwordEncoder.encode(request.getPassword()))
				.build();

		user = userRepository.save(user);
		return buildAuthResponse(user);
	}

	public AuthResponse login(LoginRequest request) {
		User user = userRepository.findByPhone(request.getPhoneOrEmail())
				.or(() -> userRepository.findByEmail(request.getPhoneOrEmail()))
				.orElseThrow(() -> new RuntimeException("User not found"));

		if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
			throw new RuntimeException("Invalid password");
		}

		return buildAuthResponse(user);
	}

	public User getCurrentUser(String userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("User not found"));
	}

	public UserProfileResponse toProfileResponse(User user) {
		return UserProfileResponse.builder()
				.userId(user.getId())
				.name(user.getName())
				.phone(user.getPhone())
				.email(user.getEmail())
				.createdAt(user.getCreatedAt())
				.build();
	}

	private AuthResponse buildAuthResponse(User user) {
		return AuthResponse.builder()
				.userId(user.getId())
				.name(user.getName())
				.phone(user.getPhone())
				.email(user.getEmail())
				.accessToken(jwtTokenProvider.generateAccessToken(user))
				.refreshToken(jwtTokenProvider.generateRefreshToken(user))
				.build();
	}
}
