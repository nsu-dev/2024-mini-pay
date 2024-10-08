package org.c4marathon.assignment.domain.account.exception;

import static org.c4marathon.assignment.domain.account.entity.responsemsg.AccountErrCode.*;

import java.util.List;
import java.util.stream.Collectors;

import org.c4marathon.assignment.domain.account.controller.AccountController;
import org.c4marathon.assignment.domain.account.dto.response.AccountErrDto;
import org.c4marathon.assignment.domain.user.dto.response.UserErrDto;
import org.c4marathon.assignment.domain.user.exception.UserException;
import org.c4marathon.assignment.global.exception.ErrDtoMapper;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.RequiredArgsConstructor;

@RestControllerAdvice(basePackageClasses = AccountController.class)
@RequiredArgsConstructor
public class AccountExceptionHandler {
	private final ErrDtoMapper errDtoMapper;

	@ExceptionHandler({AccountException.class})
	protected ResponseEntity<AccountErrDto> handleAccountException(AccountException ex) {
		return errDtoMapper.getAccountErrDto(ex.getAccountErrCode());
	}

	@ExceptionHandler({MethodArgumentNotValidException.class})
	protected ResponseEntity<AccountErrDto> handleUserInvalidException(MethodArgumentNotValidException ex) {
		List<String> invalidMsgList = ex.getBindingResult().getFieldErrors().stream()
			.map(DefaultMessageSourceResolvable::getDefaultMessage)
			.collect(Collectors.toList());

		String invalidMsg = String.join(", ", invalidMsgList);
		return errDtoMapper.getErrDto(ACCOUNT_INVALID_FAIL, invalidMsg);
	}

	@ExceptionHandler({UserException.class})
	protected ResponseEntity<UserErrDto> handleUserException(UserException ex) {
		return errDtoMapper.getUserErrDto(ex.getUserErrCode());
	}

	@ExceptionHandler({RuntimeException.class})
	protected ResponseEntity<AccountErrDto> handleAccountRuntimeException() {
		return errDtoMapper.getAccountErrDto(ACCOUNT_SERVER_ERROR);
	}
}
