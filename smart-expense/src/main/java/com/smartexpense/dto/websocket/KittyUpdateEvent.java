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
public class KittyUpdateEvent {

	private String tripId;
	private Double kittyBalance;
	private Double kittyFundedPercent;
	private String depositorUserId;
	private String depositorName;
	private Double depositAmount;
	private LocalDateTime timestamp;
}
