package org.c4marathon.assignment.domain.account.exception;

import org.c4marathon.assignment.domain.account.controller.AccountController;
import org.c4marathon.assignment.domain.account.dto.AccountErrDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = AccountController.class)
public class AccountExceptionHandler {
	@ExceptionHandler({AccountException.class})
	protected ResponseEntity<AccountErrDto> handleAccountException(AccountException ex) {
		HttpStatus httpStatus;
		try {
			httpStatus = HttpStatus.valueOf(ex.getAccountErrCode().getStatus());
		} catch (IllegalAccessError e) {
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		AccountErrDto errDto = new AccountErrDto(
			ex.getAccountErrCode().getStatus(),
			ex.getAccountErrCode().getMessage(),
			httpStatus
		);
		return new ResponseEntity<>(errDto, httpStatus);
	}
}
