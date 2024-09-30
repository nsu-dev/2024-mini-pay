package org.c4marathon.assignment.domain.account.entity.account;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public enum AccountRole {
	MAIN("메인"),
	SAVINGS("적금"),
	OTHERS("그 외");

	private final String accountRole;
}
