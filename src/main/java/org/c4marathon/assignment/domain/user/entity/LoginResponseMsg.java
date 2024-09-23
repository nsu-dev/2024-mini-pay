package org.c4marathon.assignment.domain.user.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public enum LoginResponseMsg {
	FAIL("아이디 혹은 비밀번호가 다릅니다."),
	SUCCESS("로그인 성공!"),
	USER_NOT_FOUND("유저 정보가 없습니다."),
	USER_RE_LOGIN("다시 로그인 해주세요."),
	USER_SERVER_ERROR("서버에러 입니다. 다시 시도 해주세요.");

	private final String responseMsg;
}
