package org.c4marathon.assignment.domain.account.entity;

public enum CreateResponseMsg {
	SUCCESS("계좌생성 성공!"),
	FAIL("계좌생성 실패"),
	NOUSER("유저 정보가 없습니다.");
	private final String responseMsg;

	CreateResponseMsg(String responseMsg) {
		this.responseMsg = responseMsg;
	}

	public String getResponseMsg() {
		return responseMsg;
	}
}
