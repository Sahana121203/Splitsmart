package com.smartexpense.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberBalance {

	private String userId;
	private String userName;
	private Double contribution;
	private Double consumption;
	private Double externalPaid;
	private Double finalBalance;
}
