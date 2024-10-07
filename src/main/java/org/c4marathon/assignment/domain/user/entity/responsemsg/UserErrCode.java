package org.c4marathon.assignment.domain.user.entity.responsemsg;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum UserErrCode {
	USER_INVALID_FAIL(400, JoinResponseMsg.INVALID_FAIL.getResponseMsg()),
	USER_DUPLICATED_FAIL(400, JoinResponseMsg.DUPLICATEDFAIL.getResponseMsg()),
	USER_LOGIN_FAIL(400, LoginResponseMsg.FAIL.getResponseMsg()),
	USER_SESSION_ERR(401, LoginResponseMsg.USER_RE_LOGIN.getResponseMsg()),

	USER_NOT_FOUND(404, LoginResponseMsg.USER_NOT_FOUND.getResponseMsg()),
	USER_SERVER_ERROR(500, LoginResponseMsg.USER_SERVER_ERROR.getResponseMsg());
	private final int status;
	private final String message;
}
