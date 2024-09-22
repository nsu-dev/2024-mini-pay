package org.c4marathon.assignment.common.exception.runtime;

import lombok.Getter;

@Getter
public class CustomJwtException extends RuntimeException {

	private final String code;

	public CustomJwtException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.code = errorCode.getCode();
	}
}