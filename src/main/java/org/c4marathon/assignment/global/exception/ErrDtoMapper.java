package org.c4marathon.assignment.global.exception;

import org.c4marathon.assignment.domain.account.dto.response.AccountErrDto;
import org.c4marathon.assignment.domain.account.entity.responseMsg.AccountErrCode;
import org.c4marathon.assignment.domain.settlement.dto.response.SettlementErrDto;
import org.c4marathon.assignment.domain.settlement.entity.responsemsg.SettlementErrCode;
import org.c4marathon.assignment.domain.user.dto.response.UserErrDto;
import org.c4marathon.assignment.domain.user.entity.UserErrCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class ErrDtoMapper {
	public ResponseEntity<AccountErrDto> getAccountErrDto(AccountErrCode errCode) {
		return getErrDto(errCode, errCode.getMessage());
	}

	public ResponseEntity<UserErrDto> getUserErrDto(UserErrCode errCode) {
		return getErrDto(errCode, errCode.getMessage());
	}

	public ResponseEntity<SettlementErrDto> getSettlementErrDto(SettlementErrCode errCode) {
		return getErrDto(errCode, errCode.getMessage());
	}

	public ResponseEntity<AccountErrDto> getErrDto(AccountErrCode errCode, String errMsg) {
		HttpStatus httpStatus;
		try {
			httpStatus = HttpStatus.valueOf(errCode.getStatus());
		} catch (IllegalArgumentException e) {
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		AccountErrDto errDto = new AccountErrDto(
			errCode.getStatus(),
			errMsg != null ? errMsg : errCode.getMessage(),
			httpStatus
		);
		return new ResponseEntity<>(errDto, httpStatus);
	}

	public ResponseEntity<UserErrDto> getErrDto(UserErrCode errCode, String errMsg) {
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

	public ResponseEntity<SettlementErrDto> getErrDto(SettlementErrCode errCode, String errMsg) {
		HttpStatus httpStatus;
		try {
			httpStatus = HttpStatus.valueOf(errCode.getStatus());
		} catch (IllegalArgumentException e) {
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		}
		SettlementErrDto errDto = new SettlementErrDto(
			errCode.getStatus(),
			errMsg != null ? errMsg : errCode.getMessage(),
			httpStatus
		);
		return new ResponseEntity<>(errDto, httpStatus);
	}
}
