package com.smartexpense.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KittyResponse {

	private String tripId;
	private Double kittyTarget;
	private Double kittyBalance;
	private Double kittyFundedPercent;
	private boolean readyToActivate;
	private List<MemberContributionResponse> memberContributions;
	private LocalDateTime lastDepositAt;
}
