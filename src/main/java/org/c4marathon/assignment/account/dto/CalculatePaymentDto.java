package org.c4marathon.assignment.account.dto;

import java.util.List;

import org.c4marathon.assignment.account.enums.PaymentType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CalculatePaymentDto(
	@NotNull(message = "정산할 계정이 하나라도 있어야 합니다")
	List<String> usersId,
	@Min(value = 0, message = "정산 금액은 0원 이상이여야 합니다.")
	int paymentMoney,
	@NotNull(message = "정산 타입을 하나 정해주셔야 합니다.")
	PaymentType paymentType
) {
}
