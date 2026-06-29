package com.smartexpense.dto.response;

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
public class ParticipantResponse {

	private String userId;
	private String userName;
	private Double share;
}
