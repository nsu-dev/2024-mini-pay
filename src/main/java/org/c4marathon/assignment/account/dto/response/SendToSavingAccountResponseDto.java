package org.c4marathon.assignment.account.dto.response;

public record SendToSavingAccountResponseDto(
	Long toAccountId,
	String toAccountType,
	int toAccountMoney,
	Long fromAccountId,
	String fromAccountType,
	int fromAccountMoney
) {
}
