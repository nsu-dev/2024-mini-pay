package org.c4marathon.assignment.account.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SavingAccountPwDto(
	@NotNull(message = "계좌 비밀번호는 필수 입력 항목입니다.")
	@Min(value = 1000, message = "비밀번호는 0으로 시작할 수 없습니다.")
	@Max(value = 9999, message = "비밀번호는 4자리입니다.")
	int accountPw
) {
}
