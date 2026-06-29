package com.smartexpense.config;

import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.smartexpense.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

	private final JwtTokenProvider jwtTokenProvider;

	@Override
	public boolean beforeHandshake(
			ServerHttpRequest request,
			ServerHttpResponse response,
			WebSocketHandler wsHandler,
			Map<String, Object> attributes) {
		if (!(request instanceof ServletServerHttpRequest servletRequest)) {
			return false;
		}

		String token = servletRequest.getServletRequest().getParameter("token");
		if (!StringUtils.hasText(token)) {
			return false;
		}
		if (!jwtTokenProvider.validateToken(token)) {
			return false;
		}

		String userId = jwtTokenProvider.getUserIdFromToken(token);
		attributes.put("userId", userId);
		return true;
	}

	@Override
	public void afterHandshake(
			ServerHttpRequest request,
			ServerHttpResponse response,
			WebSocketHandler wsHandler,
			Exception exception) {
		// no-op
	}
}
