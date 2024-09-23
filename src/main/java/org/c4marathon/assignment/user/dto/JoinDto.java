package org.c4marathon.assignment.user.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record JoinDto (
        @NotBlank(message = "아이디는 필수 입력 항목입니다.")
        String userId,

        @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
        String userPw,

        @NotBlank(message = "이름은 필수 입력 항목입니다.")
        String name,

        @NotBlank(message = "계좌 비밀번호는 필수 입력 항목입니다.")
        @Min(value = 1000, message = "비밀번호는 0으로 시작할 수 없습니다")
        @Max(value = 9999, message = "비밀번호가 4자리를 넘어갈 수 없습니다")
        int accountPw
){
}
