package com.smartexpense.dto.response;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseSummaryResponse {

	private String tripId;
	private int totalExpenses;
	private Double totalAmount;
	private Double myTotalShare;
	private Map<String, Double> categoryBreakdown;
	private Map<String, Double> memberSpendBreakdown;
	private List<ExpenseResponse> expenses;
}
