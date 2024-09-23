package org.c4marathon.assignment.domain.account.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public enum RemittanceResponseMsg {
	DAILYCHARGELIMIT_ERR("충전 한도 초과!"),
	SUCCESS("송금 완료!"),
	NOSUCHACCOUNT("없는 계좌!"),

	INSUFFICIENT_BALANCE("잔액 부족!"),
	NOT_MAIN_ACCOUNT("메인계좌가 아닙니다."),
	INVALID_FAIL("공백일 수 없습니다.");

	private final String responseMsg;
}
