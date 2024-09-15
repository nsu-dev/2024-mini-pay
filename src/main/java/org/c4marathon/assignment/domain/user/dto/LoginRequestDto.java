package org.c4marathon.assignment.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginRequestDto {
	@NotBlank
	private String userPhone;
	@NotBlank
	private String userPassword;
}
