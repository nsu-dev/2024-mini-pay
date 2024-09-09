package org.c4marathon.assignment.domain.account.entity;

public enum AccountRole {
	MAIN("메인"),
	SAVINGS("적금"),
	OTHERS("그 외");

	private final String accountRole;

	AccountRole(String accountRole) {
		this.accountRole = accountRole;
	}

	public String getAccountRole() {
		return accountRole;
	}
}
