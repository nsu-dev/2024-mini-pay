package org.c4marathon.assignment.account.dto.response;

public record ChargeResponseDto(
	Long accountId,
	int chargedAmount,
	int limitAmount
) {
}
