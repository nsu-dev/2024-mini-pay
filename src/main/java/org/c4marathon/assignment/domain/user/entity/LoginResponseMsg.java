package org.c4marathon.assignment.domain.user.entity;

public enum LoginResponseMsg {
	FAIL("아이디 혹은 비밀번호가 다릅니다."),
	SUCCESS("로그인 성공!"),
	NOTUSER("가입된 정보가 없습니다.");
	private final String responseMsg;

	LoginResponseMsg(String responseMsg) {
		this.responseMsg = responseMsg;
	}

	public String getResponseMsg() {
		return responseMsg;
	}
}
