package org.c4marathon.assignment.domain.user.entity;

public enum JoinResponseMsg {
	SUCCESS("회원가입 성공!"),
	INVALID_FAIL("공백이 들어갈 수 없습니다."),
	DUPLICATIEDFAIL("이미 가입된 회원입니다.");

	private final String responseMsg;

	JoinResponseMsg(String responseMsg) {
		this.responseMsg = responseMsg;
	}

	public String getResponseMsg() {
		return responseMsg;
	}
}
