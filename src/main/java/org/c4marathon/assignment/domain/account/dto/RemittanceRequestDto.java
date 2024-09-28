package org.c4marathon.assignment.domain.account.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RemittanceRequestDto(
	@NotNull(message = "계좌번호는 공백일 수 없습니다.")
	Long accountNum,
	@NotNull(message = "송금액은 공백일 수 없습니다.")
	@Min(value = 0, message = "송금액은 0원 이하가 될 수 없습니다.") @Max(value = 3_000_000, message = "금일 한도 초과 입니다.")
	Long remittanceAmount
) {
}
