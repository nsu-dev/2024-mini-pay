package org.c4marathon.assignment.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginRequestDto {
	@NotBlank
	private String userPhone;
	@NotBlank
	private String userPassword;

	public LoginRequestDto(String userPhone, String userPassword) {
		this.userPhone = userPhone;
		this.userPassword = userPassword;
	}
}
