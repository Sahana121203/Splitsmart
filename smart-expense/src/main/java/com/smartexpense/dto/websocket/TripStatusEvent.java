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
public class TripStatusEvent {

	private String tripId;
	private String tripName;
	private String oldStatus;
	private String newStatus;
	private String triggeredByUserId;
	private String triggeredByUserName;
	private LocalDateTime timestamp;
}
