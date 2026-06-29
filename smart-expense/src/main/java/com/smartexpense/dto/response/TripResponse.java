package com.smartexpense.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.smartexpense.model.enums.TripStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripResponse {

	private String tripId;
	private String name;
	private String destination;
	private LocalDate startDate;
	private LocalDate endDate;
	private TripStatus status;
	private String organizerId;
	private String organizerName;
	private Double kittyTarget;
	private Double kittyBalance;
	private Double kittyFundedPercent;
	private String baseCurrency;
	private int memberCount;
	private LocalDateTime createdAt;
	private String lateJoinerWarning;
}
