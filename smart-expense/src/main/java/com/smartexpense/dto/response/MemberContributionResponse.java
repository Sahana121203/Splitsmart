package com.smartexpense.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberContributionResponse {

	private String userId;
	private String name;
	private Double contributed;
	private Double expectedShare;
	private Double remainingShare;
	private boolean fullyContributed;
}
