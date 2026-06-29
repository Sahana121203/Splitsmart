package com.smartexpense.dto.response;

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
public class SettlementPreviewResponse {

	private String tripId;
	private String tripName;
	private SettlementMode mode;
	private Double totalContribution;
	private Double totalConsumption;
	private Double surplus;
	private int totalTransfers;
	private List<MemberSettlementResponse> memberBalances;
	private List<TransferInstructionResponse> transferInstructions;
	private boolean canFinalise;
	private String statusMessage;
}
