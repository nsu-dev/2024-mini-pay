package org.c4marathon.assignment.account.domain;

import java.util.Arrays;

import org.c4marathon.assignment.account.exception.AccountErrorCode;
import org.c4marathon.assignment.common.exception.runtime.BaseException;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AccountType {

	MAIN_ACCOUNT("메인계좌"),
	SAVING_ACCOUNT("적금계좌");

	private final String type;

	public static AccountType from(String input) {
		return Arrays.stream(values())
			.filter(type -> type.isEqual(input))
			.findAny()
			.orElseThrow(() -> new BaseException(AccountErrorCode.NOT_FOUND_ACCOUNT_TYPE));
	}

	private boolean isEqual(String input) {
		return input.equals(this.type);
	}

}
