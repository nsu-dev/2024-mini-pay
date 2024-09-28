package org.c4marathon.assignment.domain.account.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SavingRequestDto(
	@NotNull(message = "송금액은 공백일 수 없습니다.")
	@Min(value = 0, message = "송금액은 0원이하일 수 없습니다.")
	int amount
) {
}
