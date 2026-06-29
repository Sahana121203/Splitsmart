package com.smartexpense.dto.websocket;

import java.time.LocalDateTime;

import com.smartexpense.model.enums.SettlementMode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementEvent {

	private String tripId;
	private String eventType;
	private SettlementMode mode;
	private int totalTransfers;
	private String triggeredByUserId;
	private String triggeredByUserName;
	private LocalDateTime timestamp;
}
