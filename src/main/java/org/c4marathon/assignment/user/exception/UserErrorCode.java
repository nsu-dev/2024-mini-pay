package org.c4marathon.assignment.user.exception;

import org.c4marathon.assignment.common.exception.runtime.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

	DUPLICATED_EMAIL("중복된 이메일 회원이 존재합니다.", "USER_001"),
	NOT_FOUND_USER("해당 사용자를 찾을 수 없습니다.", "USER_002"),
	NOT_MATCH_PASSWORD("비밀번호가 일치하지 않습니다.", "USER_003");

	private final String message;
	private final String code;
}