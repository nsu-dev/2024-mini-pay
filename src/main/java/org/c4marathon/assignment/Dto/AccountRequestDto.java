package org.c4marathon.assignment.Dto;

import lombok.Getter;

@Getter
public class AccountRequestDto {
	private int balance;
	private String type;
	private int dailyWithdrawalLimit;

	public AccountRequestDto(int balance, String type, int dailyWithdrawalLimit) {
		this.balance = balance;
		this.type = type;
		this.dailyWithdrawalLimit = dailyWithdrawalLimit;
	}

}
