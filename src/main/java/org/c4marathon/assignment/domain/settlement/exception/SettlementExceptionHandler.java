package org.c4marathon.assignment.domain.settlement.exception;

import static org.c4marathon.assignment.domain.settlement.entity.responsemsg.SettlementErrCode.*;

import java.util.List;
import java.util.stream.Collectors;

import org.c4marathon.assignment.domain.settlement.controller.SettlementController;
import org.c4marathon.assignment.domain.settlement.dto.response.SettlementErrDto;
import org.c4marathon.assignment.domain.user.dto.response.UserErrDto;
import org.c4marathon.assignment.domain.user.exception.UserException;
import org.c4marathon.assignment.global.exception.ErrDtoMapper;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.RequiredArgsConstructor;

@RestControllerAdvice(basePackageClasses = SettlementController.class)
@RequiredArgsConstructor
public class SettlementExceptionHandler {
	private final ErrDtoMapper errDtoMapper;

	@ExceptionHandler({SettlementException.class})
	protected ResponseEntity<SettlementErrDto> handleAccountException(SettlementException ex) {
		return errDtoMapper.getSettlementErrDto(ex.getSettlementErrCode());
	}

	@ExceptionHandler({MethodArgumentNotValidException.class})
	protected ResponseEntity<SettlementErrDto> handleUserInvalidException(MethodArgumentNotValidException ex) {
		List<String> invalidMsgList = ex.getBindingResult().getFieldErrors().stream()
			.map(DefaultMessageSourceResolvable::getDefaultMessage)
			.collect(Collectors.toList());

		String invalidMsg = String.join(", ", invalidMsgList);
		return errDtoMapper.getErrDto(SETTLEMENT_INVALID_FAIL, invalidMsg);
	}

	@ExceptionHandler({UserException.class})
	protected ResponseEntity<UserErrDto> handleUserException(UserException ex) {
		return errDtoMapper.getUserErrDto(ex.getUserErrCode());
	}

	@ExceptionHandler({RuntimeException.class})
	protected ResponseEntity<SettlementErrDto> handleAccountRuntimeException() {
		return errDtoMapper.getSettlementErrDto(SETTLEMENT_SERVER_ERROR);
	}
}
