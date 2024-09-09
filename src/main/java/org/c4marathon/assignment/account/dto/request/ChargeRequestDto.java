package org.c4marathon.assignment.account.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ChargeRequestDto(

	@NotBlank(message = "계좌번호는 필수 입력 항목입니다.")
	Long accountId,
	@Min(value = 0, message = "충전금액은 0원 이상이어야 합니다.")
	int chargeAmount
) {
}
