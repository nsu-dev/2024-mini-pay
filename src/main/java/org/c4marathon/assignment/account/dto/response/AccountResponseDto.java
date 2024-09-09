package org.c4marathon.assignment.account.dto.response;

public record AccountResponseDto(

	Long id,
	String type,
	int amount,
	int limitAmount
) {
}
