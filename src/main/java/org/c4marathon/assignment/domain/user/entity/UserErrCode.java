package org.c4marathon.assignment.domain.user.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum UserErrCode {
	USER_INVALID_FAIL(400, JoinResponseMsg.INVALID_FAIL.getResponseMsg()),
	USER_DUPLICATED_FAIL(400, JoinResponseMsg.DUPLICATEDFAIL.getResponseMsg()),
	USER_LOGIN_FAIL(400, LoginResponseMsg.FAIL.getResponseMsg()),
	USER_SESSION_ERR(401, "다시 로그인 해주세요."),

	USER_NOT_FOUND(404, LoginResponseMsg.USER_NOT_FOUND.getResponseMsg()),
	USER_SERVER_ERROR(500, "서버에러 입니다. 다시 시도 해주세요.");
	private final int status;
	private final String message;
}
