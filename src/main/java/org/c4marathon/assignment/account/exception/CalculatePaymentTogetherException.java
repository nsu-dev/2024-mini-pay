package org.c4marathon.assignment.account.exception;

import org.c4marathon.assignment.common.exception.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CalculatePaymentTogetherException implements ErrorCode {
	WRONG_TYPE("타입이 잘못됐습니다.", "ACCOUNT_007");

	private final String message;
	private final String code;
}
