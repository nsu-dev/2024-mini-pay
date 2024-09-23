package org.c4marathon.assignment.domain.account.dto;

import static org.c4marathon.assignment.domain.account.entity.AccountErrCode.*;

import org.c4marathon.assignment.domain.account.exception.AccountException;

public record RemittanceRequestDto (Long accountNum, Long remittanceAmount){
	public RemittanceRequestDto{
		if(remittanceAmount > 3_000_000){
			throw new AccountException(ACCOUNT_DALIYCHARGELIMIT_ERR);
		}
	}

}
