package org.c4marathon.assignment.domain.settlement.exception;

import org.c4marathon.assignment.domain.settlement.entity.responsemsg.SettlementErrCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class SettlementException extends RuntimeException {
	private final SettlementErrCode settlementErrCode;
}
