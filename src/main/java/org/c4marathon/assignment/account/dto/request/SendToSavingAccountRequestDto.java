package org.c4marathon.assignment.account.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SendToSavingAccountRequestDto(
	@NotNull(message = "계좌번호는 필수 입력 항목입니다.")
	Long toAccountId,
	@Min(value = 0, message = "송금금액은 0원 이상이어야 합니다.")
	int remittanceMoney,
	@NotNull(message = "계좌번호는 필수 입력 항목입니다.")
	Long fromAccountId
) {
}
