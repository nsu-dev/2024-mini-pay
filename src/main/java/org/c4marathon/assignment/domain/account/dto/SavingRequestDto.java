package org.c4marathon.assignment.domain.account.dto;

import static org.c4marathon.assignment.domain.account.entity.AccountErrCode.*;

import org.c4marathon.assignment.domain.account.exception.AccountException;

import jakarta.validation.constraints.NotNull;

public record SavingRequestDto(
	@NotNull(message = "송금액은 공백일 수 없습니다.")
	int amount
) {
	public SavingRequestDto{
		if(amount <= 0){
			throw new AccountException(ACCOUNT_INVALID_FAIL);
		}
	}
}
