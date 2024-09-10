package org.c4marathon.assignment.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class UserDto {
	@NotBlank
	private String userPhone;
	@NotBlank
	private String userPassword;

	@NotBlank
	private String userName;

	@NotBlank
	private String userBirth;

	public UserDto(@NotBlank String userPhone, @NotBlank String userName, @NotBlank String userBirth,
		@NotBlank String userPassword) {
		this.userPhone = userPhone;
		this.userName = userName;
		this.userBirth = userBirth;
		this.userPassword = userPassword;
	}
}
