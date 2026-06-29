package com.smartexpense.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.smartexpense.model.enums.ExpenseCategory;
import com.smartexpense.model.enums.PaymentSource;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseResponse {

	private String expenseId;
	private String tripId;
	private String title;
	private Double amount;
	private String currency;
	private Double amountInBase;
	private ExpenseCategory category;
	private String paidByUserId;
	private String paidByUserName;
	private PaymentSource paidFrom;
	private String receiptUrl;
	private Boolean editPending;
	private Boolean deleted;
	private List<ParticipantResponse> participants;
	private Double totalParticipantShares;
	private LocalDateTime createdAt;
	private LocalDateTime editedAt;
}
