package org.c4marathon.assignment.domain.settlement.exception;

import org.c4marathon.assignment.domain.account.entity.responseMsg.AccountErrCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class SettlementException extends RuntimeException {
	private final AccountErrCode accountErrCode;
}
