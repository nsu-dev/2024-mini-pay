package org.c4marathon.assignment.domain.account.dto;

import static org.c4marathon.assignment.domain.account.entity.AccountErrCode.*;

import org.c4marathon.assignment.domain.account.exception.AccountException;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RemittanceRequestDto (
	@NotNull(message = "계좌번호는 공백일 수 없습니다.")
	Long accountNum,
	@NotNull(message = "송금액은 공백일 수 없습니다.")
	Long remittanceAmount
){
	public RemittanceRequestDto{
		if(remittanceAmount > 3_000_000){
			throw new AccountException(ACCOUNT_DALIYCHARGELIMIT_ERR);
		}
	}

}
