package org.c4marathon.assignment.domain.account.entity;

public enum AccountStatus {
	AVAILABLE("거래가능"),
	UNAVAILABLE("거래불가능");

	private final String accountStatus;

	AccountStatus(String accountStatus) {
		this.accountStatus = accountStatus;
	}

	public String getAccountStatus() {
		return accountStatus;
	}
}
