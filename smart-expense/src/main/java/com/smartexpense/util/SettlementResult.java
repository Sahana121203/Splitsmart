package com.smartexpense.util;

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
public class SettlementResult {

	private List<MemberBalance> memberBalances;
	private List<TransferInstruction> transferInstructions;
	private SettlementMode mode;
	private Double totalContribution;
	private Double totalConsumption;
	private Double surplus;
	private int totalTransfers;
}
