package com.smartexpense.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferInstruction {

	private String fromUserId;
	private String fromUserName;
	private String toUserId;
	private String toUserName;
	private Double amount;
}
