package com.smartexpense.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTripRequest {

	@NotBlank
	private String name;

	private String destination;

	private LocalDate startDate;

	private LocalDate endDate;

	@Builder.Default
	private String baseCurrency = "INR";

	@Builder.Default
	private Double kittyTarget = 0.0;
}
