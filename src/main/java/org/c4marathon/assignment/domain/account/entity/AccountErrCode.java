package org.c4marathon.assignment.domain.account.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public enum AccountErrCode {
	ACCOUNT_UNAVAILABLE(400, AccountStatus.UNAVAILABLE.getAccountStatus()),
	ACCOUNT_DALIYCHARGELIMIT_ERR(400, RemittanceResponseMsg.DAILYCHARGELIMIT_ERR.getResponseMsg()),
	ACCOUNT_INSUFFICIENT_BALANCE(400, RemittanceResponseMsg.INSUFFICIENT_BALANCE.getResponseMsg()),
	INVALID_ACCOUNT_TYPE(400, CreateResponseMsg.INVALID_ACCOUNT_TYPE.getResponseMsg()),

	ACCOUNT_USER_NOT_FOUND(404, CreateResponseMsg.NOUSER.getResponseMsg()),
	ACCOUNT_NOT_FOUND(404, RemittanceResponseMsg.NOSUCHACCOUNT.getResponseMsg()),

	ACCOUNT_CREATE_FAIL(500, CreateResponseMsg.FAIL.getResponseMsg()),
	ACCOUNT_SERVER_ERROR(500, CreateResponseMsg.ACCOUNT_SERVER_ERROR.getResponseMsg());
	private final int status;
	private final String message;
}