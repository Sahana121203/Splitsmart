package com.smartexpense.security;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.smartexpense.model.User;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Component
public class JwtTokenProvider {

	@Value("${app.jwt.secret}")
	private String secret;

	@Value("${app.jwt.expiration-ms:86400000}")
	private long expirationMs;

	@Value("${app.jwt.refresh-expiration-ms:604800000}")
	private long refreshExpirationMs;

	private SecretKey secretKey;

	@PostConstruct
	void init() {
		if (secret == null || secret.length() < 32) {
			throw new IllegalStateException("app.jwt.secret must be at least 32 characters");
		}
		secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}

	public String generateAccessToken(User user) {
		Date now = new Date();
		Date expiry = new Date(now.getTime() + expirationMs);
		return Jwts.builder()
				.setSubject(user.getId())
				.claim("phone", user.getPhone())
				.setIssuedAt(now)
				.setExpiration(expiry)
				.signWith(secretKey)
				.compact();
	}

	public String generateRefreshToken(User user) {
		Date now = new Date();
		Date expiry = new Date(now.getTime() + refreshExpirationMs);
		return Jwts.builder()
				.setSubject(user.getId())
				.setIssuedAt(now)
				.setExpiration(expiry)
				.signWith(secretKey)
				.compact();
	}

	public String getUserIdFromToken(String token) {
		return parseClaims(token).getSubject();
	}

	public String getPhoneFromToken(String token) {
		return parseClaims(token).get("phone", String.class);
	}

	public boolean validateToken(String token) {
		try {
			parseClaims(token);
			return true;
		} catch (JwtException | IllegalArgumentException ex) {
			return false;
		}
	}

	private Claims parseClaims(String token) {
		return Jwts.parserBuilder()
				.setSigningKey(secretKey)
				.build()
				.parseClaimsJws(token)
				.getBody();
	}
}
