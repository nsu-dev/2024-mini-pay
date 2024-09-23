package org.c4marathon.assignment.account.dto;

import jakarta.validation.constraints.NotBlank;

public record SavingAccountPwDto(
        @NotBlank(message = "계좌 비밀번호는 필수 입력 항목입니다.")
        int accountPw
) {
}
