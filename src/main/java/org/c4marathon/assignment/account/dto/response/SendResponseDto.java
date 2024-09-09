package org.c4marathon.assignment.account.dto.response;

public record SendResponseDto(
	Long toAccountId,
	String toAccountType,
	int toAccountMoney,
	Long fromAccountId,
	String fromAccountType,
	int fromAccountMoney
) {
}
