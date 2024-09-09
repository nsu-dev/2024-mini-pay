package org.c4marathon.assignment.account.dto.request;

public record SendRequestDto(

	Long toAccountId,
	String toAccountType,
	int sendToMoney,
	Long fromAccountId,
	String fromAccountType

) {
}
