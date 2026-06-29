package com.smartexpense.dto.request;

import com.smartexpense.model.enums.PaymentMethod;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KittyDepositRequest {

	@NotNull
	@Min(1)
	private Double amount;

	@NotNull
	private PaymentMethod method;

	private String reference;

	private String note;
}
