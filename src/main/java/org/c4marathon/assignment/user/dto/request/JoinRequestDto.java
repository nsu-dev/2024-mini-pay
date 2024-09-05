package org.c4marathon.assignment.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JoinRequestDto(
	@NotBlank(message = "이메일은 필수 입력 항목입니다.")
	@Email(message = "이메일 형식으로 입력해주세요.")
	String email,

	@NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
	@Size(min = 7, max = 12, message = "비밀번호는 7자 이상 12자 이하로 설정해주세요.")
	String password,

	@NotBlank(message = "이름은 필수 입력 항목입니다.")
	String name

) {
}
