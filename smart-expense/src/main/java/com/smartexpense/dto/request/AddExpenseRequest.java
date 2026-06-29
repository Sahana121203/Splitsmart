package com.smartexpense.dto.request;

import com.smartexpense.model.enums.ExpenseCategory;
import com.smartexpense.model.enums.PaymentSource;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddExpenseRequest {

	@NotBlank
	private String title;

	@NotNull
	@Min(1)
	private Double amount;

	@Builder.Default
	private String currency = "INR";

	@Builder.Default
	private ExpenseCategory category = ExpenseCategory.OTHER;

	private String paidByUserId;

	@Builder.Default
	private PaymentSource paidFrom = PaymentSource.KITTY;

	private String receiptUrl;

	@NotEmpty
	private java.util.List<ParticipantShareRequest> participants;

	@Builder.Default
	private Boolean equalSplit = true;
}
