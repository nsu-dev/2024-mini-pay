package org.c4marathon.assignment.account.dto.request;

public record ChargeRequestDto(

	Long accountId,
	int chargeAmount
) {
}
