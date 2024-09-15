package org.c4marathon.assignment.domain.user.exception;

import org.c4marathon.assignment.domain.user.controller.UserController;
import org.c4marathon.assignment.domain.user.dto.UserErrDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = UserController.class)
public class UserExceptionHandler {
	@ExceptionHandler({UserException.class})
	protected ResponseEntity<UserErrDto> handleAccountException(UserException ex) {
		HttpStatus httpStatus;
		try {
			httpStatus = HttpStatus.valueOf(ex.getUserErrCode().getStatus());
		} catch (IllegalAccessError e) {
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		UserErrDto errDto = new UserErrDto(
			ex.getUserErrCode().getStatus(),
			ex.getUserErrCode().getMessage(),
			httpStatus
		);
		return new ResponseEntity<>(errDto, httpStatus);
	}
}
