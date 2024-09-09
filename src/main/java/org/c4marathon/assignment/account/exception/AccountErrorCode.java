package org.c4marathon.assignment.account.exception;

import org.c4marathon.assignment.common.exception.runtime.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AccountErrorCode implements ErrorCode {

	NOT_FOUND_ACCOUNT_TYPE("해당 계좌 타입을 찾을 수 없습니다.", "ACCOUNT_001"),
	NOT_FOUND_ACCOUNT("계좌를 찾을 수 없습니다.", "ACCOUNT_002"),
	NOT_ENOUGH_AMOUNT("계좌 금액이 충분하지 않습니다.", "ACCOUNT_003"),
	NOT_AUTHORIZED_ACCOUNT("계좌 인출 권환이 없습니다.", "ACCOUNT_004"),
	NOT_ENOUGH_CHARGE_AMOUNT("충전 한도를 초과했습니다.", "ACCOUNT_005");

	private final String message;
	private final String code;
}

