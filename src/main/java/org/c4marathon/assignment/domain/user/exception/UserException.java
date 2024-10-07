package org.c4marathon.assignment.domain.user.exception;

import org.c4marathon.assignment.domain.user.entity.responsemsg.UserErrCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UserException extends RuntimeException {
	private final UserErrCode userErrCode;
}
