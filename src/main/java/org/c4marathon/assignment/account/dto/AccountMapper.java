package org.c4marathon.assignment.account.dto;

import org.c4marathon.assignment.account.domain.Account;
import org.c4marathon.assignment.account.dto.response.SavingAccountResponseDto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AccountMapper {

	public static SavingAccountResponseDto toSavingAccountResponseDto(Account account) {
		return new SavingAccountResponseDto(
			account.getType().getType(),
			account.getAmount(),
			account.getUser().getEmail(),
			account.getUser().getName()
		);
	}
}
