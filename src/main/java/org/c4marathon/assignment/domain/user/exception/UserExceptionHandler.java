package org.c4marathon.assignment.domain.user.exception;

import static org.c4marathon.assignment.domain.user.entity.UserErrCode.*;

import java.util.List;
import java.util.stream.Collectors;

import org.c4marathon.assignment.domain.user.controller.UserController;
import org.c4marathon.assignment.domain.user.dto.UserErrDto;
import org.c4marathon.assignment.domain.user.entity.UserErrCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = UserController.class)
public class UserExceptionHandler {
	@ExceptionHandler({UserException.class})
	protected ResponseEntity<UserErrDto> handleUserException(UserException ex) {
		return getUserErrDto(ex.getUserErrCode());
	}

	@ExceptionHandler({MethodArgumentNotValidException.class})
	protected ResponseEntity<UserErrDto> handleUserInvalidException(MethodArgumentNotValidException ex) {
		List<String> invalidMsgList = ex.getBindingResult().getFieldErrors().stream()
			.map(e -> e.getDefaultMessage())
			.collect(Collectors.toList());

		String invalidMsg = String.join(", ", invalidMsgList);
		return getUserErrDto(USER_INVALID_FAIL, invalidMsg);
	}

	@ExceptionHandler({RuntimeException.class})
	protected ResponseEntity<UserErrDto> handleUserRuntimeException() {
		return getUserErrDto(USER_SERVER_ERROR);
	}

	private ResponseEntity<UserErrDto> getUserErrDto(UserErrCode errCode) {
		return getUserErrDto(errCode, errCode.getMessage());
	}

	private ResponseEntity<UserErrDto> getUserErrDto(UserErrCode errCode, String errMsg) {
		HttpStatus httpStatus;
		try {
			httpStatus = HttpStatus.valueOf(errCode.getStatus());
		} catch (IllegalArgumentException e) {
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		UserErrDto errDto = new UserErrDto(
			errCode.getStatus(),
			errMsg != null ? errMsg : errCode.getMessage(),
			httpStatus
		);
		return new ResponseEntity<>(errDto, httpStatus);
	}
}
