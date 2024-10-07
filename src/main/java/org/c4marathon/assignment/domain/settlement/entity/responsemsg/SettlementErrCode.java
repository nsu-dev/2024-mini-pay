package org.c4marathon.assignment.domain.settlement.entity.responsemsg;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public enum SettlementErrCode {
	SETTLEMENT_FAIL(400, SettlementResponseMsg.REQUEST_FAIL.getResponseMsg()),
	SETTLEMENT_INVALID_TYPE(400, SettlementResponseMsg.SETTLEMENT_INVALID_TYPE.getResponseMsg()),
	SETTLEMENT_INVALID_FAIL(400, SettlementResponseMsg.SETTLEMENT_INVALID_FAIL.getResponseMsg()),
	SETTLEMENT_NOT_FOUND(404, SettlementResponseMsg.SETTLEMENT_NOT_FOUND.getResponseMsg()),
	SETTLEMENT_SERVER_ERROR(500, SettlementResponseMsg.SETTLEMENT_SERVER_ERROR.getResponseMsg());

	private final int status;
	private final String message;
}
