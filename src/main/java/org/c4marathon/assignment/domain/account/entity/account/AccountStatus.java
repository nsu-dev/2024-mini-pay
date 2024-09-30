package org.c4marathon.assignment.domain.account.entity.account;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public enum AccountStatus {
	AVAILABLE("거래가능"),
	UNAVAILABLE("거래불가능");

	private final String accountStatus;
}
