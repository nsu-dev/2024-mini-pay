package org.c4marathon.assignment.account.dto;

import java.util.List;
import java.util.stream.Collectors;

import org.c4marathon.assignment.account.domain.Account;
import org.c4marathon.assignment.account.dto.response.AccountResponseDto;
import org.c4marathon.assignment.account.dto.response.SavingAccountResponseDto;
import org.c4marathon.assignment.account.dto.response.SendResponseDto;

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

	public static List<AccountResponseDto> toAccountResponseDtos(List<Account> accounts) {
		return accounts.stream()
			.map(AccountMapper::toAccountResponse)
			.collect(Collectors.toList());
	}

	public static SendResponseDto toSendResponseDto(Account toAccount, Account fromAccount) {
		return new SendResponseDto(
			toAccount.getId(),
			toAccount.getType().getType(),
			toAccount.getAmount(),
			fromAccount.getId(),
			fromAccount.getType().getType(),
			fromAccount.getAmount()
		);
	}

	private static AccountResponseDto toAccountResponse(Account account) {
		return new AccountResponseDto(
			account.getId(),
			account.getType().getType(),
			account.getAmount(),
			account.getLimitAmount()
		);
	}
}
