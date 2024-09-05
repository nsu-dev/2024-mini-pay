package org.c4marathon.assignment.user.exception;

import org.c4marathon.assignment.common.exception.runtime.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

	DUPLICATED_EMAIL("중복된 이메일 회원이 존재합니다.", "USER_001");

	private final String message;
	private final String code;
}
