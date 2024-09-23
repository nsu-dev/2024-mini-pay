package org.c4marathon.assignment.event;

import java.util.Random;

import org.c4marathon.assignment.account.domain.Account;
import org.c4marathon.assignment.account.domain.AccountType;
import org.c4marathon.assignment.account.repository.AccountRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class JoinEventHandler {
	private final AccountRepository accountRepository;

	Long randomAccountNum = new Random().nextLong();

	@TransactionalEventListener
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void craeteAccount(JoinEventDto joinEventDto) {
		Account account = Account.builder()
			.accountNum(randomAccountNum)
			.type(AccountType.MAIN_ACCOUNT)
			.accountPw(joinEventDto.getUser().getAccountPw())
			.limitaccount(3000000)
			.amount(0)
			.user(joinEventDto.getUser())
			.build();

		accountRepository.save(account);
	}
}
