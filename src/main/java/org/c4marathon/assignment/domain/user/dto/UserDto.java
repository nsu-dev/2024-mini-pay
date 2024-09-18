package org.c4marathon.assignment.domain.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UserDto(
	@NotBlank String userPhone,
	@NotBlank String userPassword,
	@NotBlank String userName,
	@NotBlank String userBirth
) {}
