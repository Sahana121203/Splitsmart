package com.smartexpense.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

	private String userId;
	private String name;
	private String phone;
	private String email;
	private LocalDateTime createdAt;
}
