package org.c4marathon.assignment.domain.account.exception;

import static org.c4marathon.assignment.domain.account.entity.AccountErrCode.*;
import static org.c4marathon.assignment.domain.user.entity.UserErrCode.*;

import java.util.List;
import java.util.stream.Collectors;

import org.c4marathon.assignment.domain.account.controller.AccountController;
import org.c4marathon.assignment.domain.account.dto.AccountErrDto;
import org.c4marathon.assignment.domain.account.entity.AccountErrCode;
import org.c4marathon.assignment.domain.user.dto.UserErrDto;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = AccountController.class)
public class AccountExceptionHandler {
	@ExceptionHandler({AccountException.class})
	protected ResponseEntity<AccountErrDto> handleAccountException(AccountException ex) {
		return getAccountErrDto(ex.getAccountErrCode());
	}
	@ExceptionHandler({MethodArgumentNotValidException.class})
	protected ResponseEntity<AccountErrDto> handleUserInvalidException(MethodArgumentNotValidException ex) {
		List<String> invalidMsgList = ex.getBindingResult().getFieldErrors().stream()
			.map(DefaultMessageSourceResolvable::getDefaultMessage)
			.collect(Collectors.toList());

		String invalidMsg = String.join(", ", invalidMsgList);
		return getAccountErrDto(ACCOUNT_INVALID_FAIL, invalidMsg);
	}
	@ExceptionHandler({RuntimeException.class})
	protected ResponseEntity<AccountErrDto> handleAccountRuntimeException(){
		return(getAccountErrDto(ACCOUNT_SERVER_ERROR));
	}

	private ResponseEntity<AccountErrDto> getAccountErrDto(AccountErrCode errCode){
		return getAccountErrDto(errCode, errCode.getMessage());
	}
	private ResponseEntity<AccountErrDto> getAccountErrDto(AccountErrCode errCode , String errMsg) {
		HttpStatus httpStatus;
		try {
			httpStatus = HttpStatus.valueOf(errCode.getStatus());
		} catch (IllegalAccessError e) {
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		AccountErrDto errDto = new AccountErrDto(
			errCode.getStatus(),
			errMsg != null ? errMsg : errCode.getMessage(),
			httpStatus
		);
		return new ResponseEntity<>(errDto, httpStatus);
	}
}
