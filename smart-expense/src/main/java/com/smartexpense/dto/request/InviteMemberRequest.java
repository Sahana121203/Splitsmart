package com.smartexpense.dto.request;

import org.springframework.util.StringUtils;

import com.smartexpense.model.enums.MemberRole;

import jakarta.validation.constraints.AssertTrue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InviteMemberRequest {

	private String phone;

	private String email;

	@Builder.Default
	private MemberRole role = MemberRole.MEMBER;

	@AssertTrue(message = "Either phone or email must be provided")
	public boolean isPhoneOrEmailProvided() {
		return StringUtils.hasText(phone) || StringUtils.hasText(email);
	}
}
