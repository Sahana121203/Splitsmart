package com.smartexpense.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.smartexpense.model.enums.ExpenseCategory;
import com.smartexpense.model.enums.PaymentSource;

import jakarta.validation.constraints.AssertTrue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EditExpenseRequest {

	private String title;

	private Double amount;

	private ExpenseCategory category;

	private String paidByUserId;

	private PaymentSource paidFrom;

	private String receiptUrl;

	private Boolean equalSplit;

	private java.util.List<ParticipantShareRequest> participants;

	@AssertTrue(message = "At least one field must be provided")
	@JsonIgnore
	public boolean isAtLeastOneFieldProvided() {
		return title != null || amount != null
				|| category != null || paidByUserId != null
				|| paidFrom != null || participants != null;
	}
}
