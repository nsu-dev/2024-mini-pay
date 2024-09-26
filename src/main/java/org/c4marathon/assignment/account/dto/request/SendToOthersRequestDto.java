package org.c4marathon.assignment.account.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SendToOthersRequestDto(
	@NotNull(message = "계좌번호는 필수 입력 항목입니다.")
	Long accountId,
	@Min(value = 0, message = "송금금액은 0원 이상이어야 합니다.")
	int remittanceAmount
) {
}