package org.c4marathon.assignment.domain.account.entity;

public enum RemittanceResponseMsg {
	DAILYCHARGELIMIT_ERR("충전 한도 초과!"),
	SUCCESS("송금 완료!"),
	NOSUCHACCOUNT("없는 계좌!"),

	INSUFFICIENT_BALANCE("잔액 부족!");

	private final String responseMsg;

	RemittanceResponseMsg(String responseMsg) {
		this.responseMsg = responseMsg;
	}

	public String getResponseMsg() {
		return responseMsg;
	}
}
