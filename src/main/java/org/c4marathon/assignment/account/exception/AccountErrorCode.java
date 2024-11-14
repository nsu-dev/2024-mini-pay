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
	NOT_AUTHORIZED_ACCOUNT("계좌 인출 권한이 없습니다.", "ACCOUNT_004"),
	NOT_ENOUGH_CHARGE_AMOUNT("충전 한도를 초과했습니다.", "ACCOUNT_005"),
	NOT_ACCESS_CHARGE("계좌 금액 충전에 접근할 수 없는 계좌입니다.", "ACCOUNT_006"),
	FAILED_ACCOUNT_DEPOSIT("해당 계좌에 입금을 실패했습니다.", "ACCOUNT_007"),
	NOT_MAIN_ACCOUNT("메인 계좌가 아닙니다.", "ACCOUNT_008"),
	FAILED_AUTO_CHARGING("부족한 금액 충전을 실패했습니다.", "ACCOUNT_009");
	private final String message;
	private final String code;
}
