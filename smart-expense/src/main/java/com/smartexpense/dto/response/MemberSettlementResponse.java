package com.smartexpense.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberSettlementResponse {

	private String userId;
	private String userName;
	private Double contribution;
	private Double consumption;
	private Double externalPaid;
	private Double finalBalance;
	private String role;
}
