package org.c4marathon.assignment.common.exception.runtime;

public record ExceptionResponse(
	String errorCode,
	String message
) {
}
