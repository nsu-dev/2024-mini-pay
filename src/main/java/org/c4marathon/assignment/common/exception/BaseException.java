package org.c4marathon.assignment.common.exception;

import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {
	private final String code;

	public BaseException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.code = errorCode.getCode();
	}
}
