package org.c4marathon.assignment.user.exception;

import org.c4marathon.assignment.common.exception.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LoginException implements ErrorCode {
	NOT_FOUND_USER("아이디가 일치하는 사용자를 찾을 수 없습니다.", "USER_002"),
	NOT_MATCH_PASSWORD("비밀번호가 일치하지 않습니다.", "USER_003");

	private final String message;
	private final String code;
}
