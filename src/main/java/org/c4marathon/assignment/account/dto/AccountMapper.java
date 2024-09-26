package org.c4marathon.assignment.account.dto;

import java.util.List;
import java.util.stream.Collectors;

import org.c4marathon.assignment.account.domain.Account;
import org.c4marathon.assignment.account.dto.response.AccountResponseDto;
import org.c4marathon.assignment.account.dto.response.ChargeResponseDto;
import org.c4marathon.assignment.account.dto.response.SavingAccountResponseDto;
import org.c4marathon.assignment.account.dto.response.SendToOthersResponseDto;
import org.c4marathon.assignment.account.dto.response.SendToSavingAccountResponseDto;
import org.c4marathon.assignment.user.domain.User;

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

	public static SendToSavingAccountResponseDto toSendResponseDto(Account toAccount, Account fromAccount) {
		return new SendToSavingAccountResponseDto(
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

	public static ChargeResponseDto toChargeResponseDto(Account findAccount) {
		return new ChargeResponseDto(
			findAccount.getId(),
			findAccount.getAmount(),
			findAccount.getLimitAmount()
		);
	}

	public static SendToOthersResponseDto sendToOthersResponseDto(User user, int sentAmount) {
		return new SendToOthersResponseDto(
			user.getName(),
			sentAmount
		);
	}
}
