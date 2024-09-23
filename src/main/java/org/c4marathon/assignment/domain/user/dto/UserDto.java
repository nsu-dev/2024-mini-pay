package org.c4marathon.assignment.domain.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UserDto(
	@NotBlank(message = "전화번호는 공백일 수 없습니다.")
	String userPhone,
	@NotBlank(message = "비밀번호는 공백일 수 없습니다.")
	String userPassword,
	@NotBlank(message = "이름은 공백일 수 없습니다.")
	String userName,
	@NotBlank(message = "생년월일은 공백일 수 없습니다.")
	String userBirth
) {}
