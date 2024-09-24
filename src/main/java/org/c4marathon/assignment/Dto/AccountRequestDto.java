package org.c4marathon.assignment.Dto;

import org.c4marathon.assignment.domain.AccountType;

import lombok.Getter;

@Getter
public class AccountRequestDto {
	private int balance;
	private AccountType type;
	private int dailyWithdrawalLimit;

	public AccountRequestDto(int balance, AccountType type, int dailyWithdrawalLimit) {
		this.balance = balance;
		this.type = type;
		this.dailyWithdrawalLimit = dailyWithdrawalLimit;
	}

}
