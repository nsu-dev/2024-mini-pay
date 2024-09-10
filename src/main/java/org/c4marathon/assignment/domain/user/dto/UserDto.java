package org.c4marathon.assignment.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public class UserDto {
	@NotBlank
	private String userPhone;
	@NotBlank
	private String password;

	@NotBlank
	private String userName;

	@NotBlank
	private String userBirth;

	public UserDto(@NotBlank String userPhone, @NotBlank String userName, @NotBlank String userBirth,
		@NotBlank String password) {
		this.userPhone = userPhone;
		this.userName = userName;
		this.userBirth = userBirth;
		this.password = password;
	}
}
