package org.c4marathon.assignment.event.account;

import org.c4marathon.assignment.account.domain.Account;

import lombok.Getter;

@Getter
public class WithdrawalFailEvent {

	private Account account;
	private int withdrawnAmount;

	public WithdrawalFailEvent(Account account, int withdrawnAmount) {
		this.account = account;
		this.withdrawnAmount = withdrawnAmount;
	}
}
