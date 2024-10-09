package org.c4marathon.assignment.Dto;

import org.c4marathon.assignment.domain.AccountType;

import lombok.Builder;
import lombok.Getter;

@Getter
public class AccountResponseDto {
	private Long accountId;
	private int balance;
	//일반계좌인지 적금계좌인지
	private AccountType type;
	private int dailyWithdrawalLimit;

	@Builder
	public AccountResponseDto(Long accountId, int balance, AccountType type, int dailyWithdrawalLimit) {
		this.accountId = accountId;
		this.balance = balance;
		this.type = type;
		this.dailyWithdrawalLimit = dailyWithdrawalLimit;
	}
}
