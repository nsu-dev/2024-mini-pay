package org.c4marathon.assignment.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class UserDto {
	@NotBlank(message = "공백이 들어갈 수 없습니다.")
	private String userPhone;
	@NotBlank(message = "공백이 들어갈 수 없습니다.")
	private String userPassword;

	@NotBlank(message = "공백이 들어갈 수 없습니다.")
	private String userName;

	@NotBlank(message = "공백이 들어갈 수 없습니다.")
	private String userBirth;

	public UserDto(@NotBlank String userPhone, @NotBlank String userName, @NotBlank String userBirth,
		@NotBlank String userPassword) {
		this.userPhone = userPhone;
		this.userName = userName;
		this.userBirth = userBirth;
		this.userPassword = userPassword;
	}
}
