package com.smartexpense.dto.response;

import java.time.LocalDateTime;

import com.smartexpense.model.enums.ExpenseCategory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingEditResponse {

	private String expenseId;
	private String expenseTitle;
	private Double currentAmount;
	private ExpenseCategory currentCategory;
	private String requestedByUserId;
	private String requestedByUserName;
	private String proposedChangesJson;
	private LocalDateTime requestedAt;
}
