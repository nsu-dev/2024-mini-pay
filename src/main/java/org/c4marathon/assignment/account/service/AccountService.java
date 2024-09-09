package org.c4marathon.assignment.account.service;

import static org.c4marathon.assignment.account.domain.AccountType.*;

import org.c4marathon.assignment.account.domain.Account;
import org.c4marathon.assignment.account.dto.AccountMapper;
import org.c4marathon.assignment.account.dto.response.SavingAccountResponseDto;
import org.c4marathon.assignment.account.repository.AccountRepository;
import org.c4marathon.assignment.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {

	private final AccountRepository accountRepository;

	@Transactional
	public SavingAccountResponseDto generateSavingAccount(User user) {
		Account savingAccount = Account.builder()
			.type(SAVING_ACCOUNT)
			.amount(0)
			.limitAmount(3_000_000)
			.user(user)
			.build();

		accountRepository.save(savingAccount);
		return AccountMapper.toSavingAccountResponseDto(savingAccount);
	}
}
