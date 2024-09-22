package org.c4marathon.assignment.account.dto.request;

public record SendToOthersRequestDto(
	Long accountId,
	String accountType,
	int sendAmount
) {
}
