package com.smartexpense.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

	@NotBlank
	private String name;

	@NotBlank
	@Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
	private String phone;

	@Email
	private String email;

	@NotBlank
	@Size(min = 6)
	private String password;
}
