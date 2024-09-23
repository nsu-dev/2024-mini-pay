package org.c4marathon.assignment.account.exception;

import org.c4marathon.assignment.common.exception.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MainAccountException implements ErrorCode {
	LIMIT_ACCOUNT("일일 충전 한도를 초과했습니다", "ACCOUNT_001"),
	SHORT_MONEY("송금 금액보다 잔액이 모자랍니다", "ACCOUNT_002");

	private final String message;
	private final String code;
}
