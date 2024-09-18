package org.c4marathon.assignment.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

public record LoginRequestDto(
	@NotBlank String userPhone,
	@NotBlank String userPassword) {}
