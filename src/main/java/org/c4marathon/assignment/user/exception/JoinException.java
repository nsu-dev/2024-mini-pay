package org.c4marathon.assignment.user.exception;

import org.c4marathon.assignment.common.exception.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum JoinException implements ErrorCode {
	FOUND_USER("아이디가 일치하는 사용자가 있습니다.", "USER_001");

	private final String message;
	private final String code;
}
