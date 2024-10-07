package org.c4marathon.assignment.domain.account.exception;

import org.c4marathon.assignment.domain.account.entity.responsemsg.AccountErrCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class AccountException extends RuntimeException {
	private final AccountErrCode accountErrCode;
}
