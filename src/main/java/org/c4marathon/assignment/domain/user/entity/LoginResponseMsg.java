package org.c4marathon.assignment.domain.user.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum LoginResponseMsg {
	FAIL("아이디 혹은 비밀번호가 다릅니다."),
	SUCCESS("로그인 성공!"),
	USER_NOT_FOUND("유저 정보가 없습니다.");

	private final String responseMsg;
}
