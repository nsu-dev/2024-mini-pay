package org.c4marathon.assignment.domain.account.service;

import java.time.LocalDateTime;
import java.util.Random;

import org.c4marathon.assignment.domain.account.entity.Account;
import org.c4marathon.assignment.domain.account.entity.AccountRole;
import org.c4marathon.assignment.domain.account.entity.AccountStatus;
import org.c4marathon.assignment.domain.account.entity.ScheduleCreateEvent;
import org.c4marathon.assignment.domain.account.repository.AccountRepository;
import org.c4marathon.assignment.domain.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {
	private final AccountRepository accountRepository;

	@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void createMain(ScheduleCreateEvent scheduleCreateEvent) {
		User user = scheduleCreateEvent.user();
		String accountNum = createRandomAccount();
		if (duplicatedAccount(accountNum)) {
			Account account = createAccount(user, accountNum, AccountRole.MAIN);
			accountRepository.save(account);
		} else {
			createMain(scheduleCreateEvent);
		}
	}

	private String createRandomAccount() {
		long min = 3000000000000L;
		long max = 3999999999999L;
		Random random = new Random();
		String accountNum = String.valueOf(min + (long)(random.nextDouble() * (max - min + 1)));

		return accountNum;
	}

	private Account createAccount(User user, String accountNum, AccountRole accountRole) {
		return Account.builder()
			.accountNum(accountNum)
			.accountRole(accountRole)
			.registeredAt(LocalDateTime.now())
			.accountBalance(0L)
			.accountStatus(AccountStatus.AVAILABLE)
			.dailyChargeLimit(0)
			.user(user)
			.build();
	}

	private boolean duplicatedAccount(String accountNum) {
		return !(accountRepository.existsByAccountNum(accountNum));
	}
}
