package org.c4marathon.assignment.domain.account.entity.responseMsg;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public enum SettlementResponseMsg {
	REQUEST_COMPLETED("정산요청이 완료되었습니다."),
	REQUEST_FAIL("정산요청이 실패하였습니다."),
	SETTLEMENT_NOT_FOUND("정산 내역이 없습니다.");
	private final String responseMsg;
}
