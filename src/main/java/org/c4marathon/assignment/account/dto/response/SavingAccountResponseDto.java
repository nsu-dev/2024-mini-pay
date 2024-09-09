package org.c4marathon.assignment.account.dto.response;

public record SavingAccountResponseDto(

	String type,
	int amount,
	String userEmail,
	String userName

) {
}
