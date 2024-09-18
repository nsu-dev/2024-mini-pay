package org.c4marathon.assignment.domain.account.exception;

import static org.c4marathon.assignment.domain.account.entity.AccountErrCode.*;

import org.c4marathon.assignment.domain.account.controller.AccountController;
import org.c4marathon.assignment.domain.account.dto.AccountErrDto;
import org.c4marathon.assignment.domain.account.entity.AccountErrCode;
import org.c4marathon.assignment.domain.user.dto.UserErrDto;
import org.c4marathon.assignment.domain.user.entity.UserErrCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = AccountController.class)
public class AccountExceptionHandler {
	@ExceptionHandler({AccountException.class})
	protected ResponseEntity<AccountErrDto> handleAccountException(AccountException ex) {
		return getAccountErrDto(ex.getAccountErrCode());
	}

	@ExceptionHandler({RuntimeException.class})
	protected ResponseEntity<AccountErrDto> handleAccountRuntimeException(){
		return(getAccountErrDto(ACCOUNT_SERVER_ERROR));
	}

	private ResponseEntity<AccountErrDto> getAccountErrDto(AccountErrCode errCode) {
		HttpStatus httpStatus;
		try {
			httpStatus = HttpStatus.valueOf(errCode.getStatus());
		} catch (IllegalAccessError e) {
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		AccountErrDto errDto = new AccountErrDto(
			errCode.getStatus(),
			errCode.getMessage(),
			httpStatus
		);
		return new ResponseEntity<>(errDto, httpStatus);
	}
}
