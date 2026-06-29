package com.smartexpense.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetResultResponse {

	private Double medianBudget;
	private Double rangeMin;
	private Double rangeMax;
	private Double avgBudget;
	private int voteCount;
	private String suggestion;
}
