package org.c4marathon.assignment.domain.settlement.entity.responsemsg;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public enum SettlementResponseMsg {
	REQUEST_COMPLETED("정산요청이 완료되었습니다."),
	REQUEST_FAIL("정산요청이 실패하였습니다."),
	SETTLEMENT_NOT_FOUND("정산 내역이 없습니다."),
	SETTLEMENT_INVALID_TYPE("정산 유형이 잘못되었습니다."),
	SETTLEMENT_INVALID_FAIL("공백일 수 없습니다."),
	SETTLEMENT_SERVER_ERROR("서버 에러 입니다. 다시 시도 부탁드립니다.");
	
	private final String responseMsg;
}
