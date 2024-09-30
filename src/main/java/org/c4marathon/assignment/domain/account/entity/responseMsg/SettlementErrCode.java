package org.c4marathon.assignment.domain.account.entity.responseMsg;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public enum SettlementErrCode {
	SETTLEMENT_FAIL(400, SettlementResponseMsg.REQUEST_FAIL.getResponseMsg()),
	SETTLEMENT_NOT_FOUND(404, SettlementResponseMsg.SETTLEMENT_NOT_FOUND.getResponseMsg());

	private final int status;
	private final String message;
}
