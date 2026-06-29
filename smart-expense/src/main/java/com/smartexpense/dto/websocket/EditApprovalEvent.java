package com.smartexpense.dto.websocket;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EditApprovalEvent {

	private String tripId;
	private String expenseId;
	private String expenseTitle;
	private String eventType;
	private String requestedByUserId;
	private String requestedByUserName;
	private String adminUserId;
	private String adminUserName;
	private LocalDateTime timestamp;
}
