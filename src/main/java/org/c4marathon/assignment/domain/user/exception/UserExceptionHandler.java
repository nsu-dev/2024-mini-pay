package org.c4marathon.assignment.domain.user.exception;

import static org.c4marathon.assignment.domain.user.entity.UserErrCode.*;

import java.util.List;
import java.util.stream.Collectors;

import org.c4marathon.assignment.domain.account.dto.response.AccountErrDto;
import org.c4marathon.assignment.domain.account.exception.AccountException;
import org.c4marathon.assignment.domain.user.controller.UserController;
import org.c4marathon.assignment.domain.user.dto.response.UserErrDto;
import org.c4marathon.assignment.global.exception.ErrDtoMapper;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.RequiredArgsConstructor;

@RestControllerAdvice(basePackageClasses = UserController.class)
@RequiredArgsConstructor
public class UserExceptionHandler {
	private final ErrDtoMapper errDtoMapper;

	@ExceptionHandler({UserException.class})
	protected ResponseEntity<UserErrDto> handleUserException(UserException ex) {
		return errDtoMapper.getUserErrDto(ex.getUserErrCode());
	}

	@ExceptionHandler({MethodArgumentNotValidException.class})
	protected ResponseEntity<UserErrDto> handleUserInvalidException(MethodArgumentNotValidException ex) {
		List<String> invalidMsgList = ex.getBindingResult().getFieldErrors().stream()
			.map(DefaultMessageSourceResolvable::getDefaultMessage)
			.collect(Collectors.toList());

		String invalidMsg = String.join(", ", invalidMsgList);
		return errDtoMapper.getErrDto(USER_INVALID_FAIL, invalidMsg);
	}

	@ExceptionHandler({AccountException.class})
	protected ResponseEntity<AccountErrDto> handleAccountException(AccountException ex) {
		return errDtoMapper.getAccountErrDto(ex.getAccountErrCode());
	}

	@ExceptionHandler({RuntimeException.class})
	protected ResponseEntity<UserErrDto> handleUserRuntimeException() {
		return errDtoMapper.getUserErrDto(USER_SERVER_ERROR);
	}
}
