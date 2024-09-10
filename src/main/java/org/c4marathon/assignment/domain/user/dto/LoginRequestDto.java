package org.c4marathon.assignment.domain.user.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequestDto {
	@NotBlank
	private String userPhone;
	@NotBlank
	private String userPassword;
}
