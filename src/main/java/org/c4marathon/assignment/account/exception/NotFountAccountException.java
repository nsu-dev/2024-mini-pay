package org.c4marathon.assignment.account.exception;

import org.c4marathon.assignment.common.exception.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotFountAccountException implements ErrorCode {
	NOT_FOUNT_ACCOUNT("계좌번호와 일치하는 계좌를 찾을 수 없습니다", "ACCOUNT_003"),
	NOT_MATCH_ACCOUNT("비밀번호와 일치하는 계좌를 찾을 수 없습니다", "ACCOUNT_004"),
	NOT_FOUNT_MAIN_ACCOUNT("아이디에 일치하는 메인 계좌를 찾을 수 없습니다", "ACCOUNT_005");

	private final String message;
	private final String code;
}
