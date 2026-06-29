package com.smartexpense.dto.response;

import java.time.LocalDateTime;

import com.smartexpense.model.enums.PaymentMethod;
import com.smartexpense.model.enums.PaymentType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentHistoryResponse {

	private String paymentId;
	private String userId;
	private String userName;
	private Double amount;
	private PaymentMethod method;
	private PaymentType type;
	private String reference;
	private String note;
	private LocalDateTime createdAt;
}
