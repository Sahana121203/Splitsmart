package com.smartexpense.dto.request;

import com.smartexpense.model.enums.TripStatus;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTripStatusRequest {

	@NotNull
	private TripStatus newStatus;
}
