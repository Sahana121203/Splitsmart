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
public class ExpenseEvent {

	private String tripId;
	private String eventType;
	private String expenseId;
	private String expenseTitle;
	private Double amount;
	private String triggeredByUserId;
	private String triggeredByUserName;
	private LocalDateTime timestamp;
}
