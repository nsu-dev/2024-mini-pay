package org.c4marathon.assignment.event.account;

import static org.c4marathon.assignment.account.domain.AccountType.*;

import org.c4marathon.assignment.account.domain.Account;
import org.c4marathon.assignment.account.repository.AccountRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountEventHandler {

	private final AccountRepository accountRepository;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void generateAccount(AccountEvent event) {
		Account newAccount = Account.builder()
			.type(MAIN_ACCOUNT)
			.amount(0)
			.limitAmount(3_000_000)
			.user(event.getUser())
			.build();

		accountRepository.save(newAccount);

		log.info("{} 회원 메인 계좌 생성 완료", event.getUser().getName());
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void cancelWithdrawal(WithdrawalFailEvent event) {
		Account withdrawnAccount = event.getAccount();
		withdrawnAccount.increaseAmount(event.getWithdrawnAmount());
	}
}
