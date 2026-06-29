package com.smartexpense.dto.response;

import java.time.LocalDateTime;

import com.smartexpense.model.enums.PaymentMethod;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalPaymentResponse {

	private String paymentId;
	private String tripId;
	private String expenseId;
	private String expenseTitle;
	private String paidByUserId;
	private String paidByUserName;
	private Double amount;
	private PaymentMethod method;
	private String reference;
	private String note;
	private LocalDateTime createdAt;
}
