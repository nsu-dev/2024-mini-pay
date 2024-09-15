package org.c4marathon.assignment.domain.account.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum AccountRole {
	MAIN("메인"),
	SAVINGS("적금"),
	OTHERS("그 외");

	private final String accountRole;
}
