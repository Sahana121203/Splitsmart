package com.smartexpense.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.smartexpense.model.enums.SettlementMode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinalSettlementResponse {

	private String tripId;
	private String tripName;
	private SettlementMode mode;
	private List<TransferInstructionResponse> transferInstructions;
	private Double totalContribution;
	private Double totalConsumption;
	private Double surplus;
	private LocalDateTime finalisedAt;
	private String message;
}
