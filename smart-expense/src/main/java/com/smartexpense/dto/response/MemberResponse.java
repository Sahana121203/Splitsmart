package com.smartexpense.dto.response;

import java.time.LocalDateTime;

import com.smartexpense.model.enums.MemberRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberResponse {

	private String memberId;
	private String userId;
	private String name;
	private String phone;
	private MemberRole role;
	private Double contribution;
	private Double consumption;
	private Double externalPaid;
	private Double netBalance;
	private LocalDateTime joinedAt;
}
