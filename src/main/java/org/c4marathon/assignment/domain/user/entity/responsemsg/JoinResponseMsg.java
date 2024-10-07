package org.c4marathon.assignment.domain.user.entity.responsemsg;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public enum JoinResponseMsg {
	SUCCESS("회원가입 성공!"),
	INVALID_FAIL("공백이 들어갈 수 없습니다."),
	DUPLICATEDFAIL("이미 가입된 회원입니다.");

	private final String responseMsg;
}
