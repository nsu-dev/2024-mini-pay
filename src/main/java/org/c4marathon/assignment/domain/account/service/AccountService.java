package org.c4marathon.assignment.domain.account.service;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Random;

import org.c4marathon.assignment.domain.account.dto.CreateResponseDto;
import org.c4marathon.assignment.domain.account.dto.RemittanceRequestDto;
import org.c4marathon.assignment.domain.account.dto.RemittanceResponseDto;
import org.c4marathon.assignment.domain.account.dto.SavingRequestDto;
import org.c4marathon.assignment.domain.account.entity.Account;
import org.c4marathon.assignment.domain.account.entity.AccountRole;
import org.c4marathon.assignment.domain.account.entity.AccountStatus;
import org.c4marathon.assignment.domain.account.entity.CreateResponseMsg;
import org.c4marathon.assignment.domain.account.entity.RemittanceResponseMsg;
import org.c4marathon.assignment.domain.account.entity.ScheduleCreateEvent;
import org.c4marathon.assignment.domain.account.repository.AccountRepository;
import org.c4marathon.assignment.domain.user.entity.User;
import org.c4marathon.assignment.domain.user.repository.UserRepository;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.client.HttpClientErrorException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {
	private final AccountRepository accountRepository;
	private final UserRepository userRepository;

	//메인계좌 생성
	@EventListener
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
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

	//계좌번호 생성
	private String createRandomAccount() {
		long min = 3000000000000L;
		long max = 3999999999999L;
		Random random = new Random();
		String accountNum = String.valueOf(min + (long)(random.nextDouble() * (max - min + 1)));

		return accountNum;
	}

	//계좌 Entity 생성
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

	//계좌 중복 검사
	private boolean duplicatedAccount(String accountNum) {
		return !(accountRepository.existsByAccountNum(accountNum));
	}

	//메인계좌 충전
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public RemittanceResponseDto chargeMain(RemittanceRequestDto remittanceRequestDto) {
		String accountNum = remittanceRequestDto.getAccountNum();
		Account account = accountRepository.findByAccountNum(accountNum);

		Long remittanceAmount = Long.parseLong(remittanceRequestDto.getRemittanceAmount());
		if (remittanceAmount > 3000000 || account.getDailyChargeLimit() >= 3000000) {
			return RemittanceResponseDto.builder()
				.responseMsg(RemittanceResponseMsg.DAILYCHARGELIMIT_ERR.getResponseMsg())
				.build();
		} else if (account.getAccountStatus() == AccountStatus.UNAVAILABLE) {
			return RemittanceResponseDto.builder()
				.responseMsg(AccountStatus.UNAVAILABLE.getAccountStatus())
				.build();
		} else if (account.getAccountStatus() == AccountStatus.AVAILABLE
			&& remittanceAmount <= 3000000
			&& account.getDailyChargeLimit() <= 3000000) {
			Long accountBalance = account.getAccountBalance() + remittanceAmount;
			int dailyChargeLimit = account.getDailyChargeLimit() + remittanceAmount.intValue();
			if (dailyChargeLimit > 3000000) {
				return RemittanceResponseDto.builder()
					.responseMsg(RemittanceResponseMsg.DAILYCHARGELIMIT_ERR.getResponseMsg())
					.build();
			}
			account.updateAccount(accountBalance, dailyChargeLimit);

			return RemittanceResponseDto.builder()
				.responseMsg(RemittanceResponseMsg.SUCCESS.getResponseMsg())
				.build();
		}
		throw new HttpClientErrorException(HttpStatusCode.valueOf(500));
	}

	//메인 외 계좌 생성
	public CreateResponseDto createAccountOther(Long userId, String createAccountRole) {
		User user = userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException());
		String accountNum = createRandomAccount();
		AccountRole accountRole = determineAccountRole(createAccountRole);
		if (duplicatedAccount(accountNum)) {
			Account account = createAccount(user, accountNum, accountRole);
			accountRepository.save(account);
			return CreateResponseDto.builder()
				.responseMsg(CreateResponseMsg.SUCCESS.getResponseMsg())
				.build();
		} else {
			return CreateResponseDto.builder()
				.responseMsg(CreateResponseMsg.FAIL.getResponseMsg())
				.build();
		}
	}

	//계좌 역할 구별
	private AccountRole determineAccountRole(String createAccountRole) {
		AccountRole accountRole = null;
		switch (createAccountRole) {
			case "SAVINGS":
				accountRole = AccountRole.SAVINGS;
				break;
			case "OTHERS":
				accountRole = AccountRole.OTHERS;
				break;
		}
		return accountRole;
	}

	//메인계좌에서 인출 후 적금계좌에 입금
	@Transactional(isolation = Isolation.REPEATABLE_READ)
	public RemittanceResponseDto savingRemittance(Long savingId, User user, SavingRequestDto savingRequestDto) {
		Account mainAccount = accountRepository.findMainAccount(user.getUserId(), AccountRole.MAIN);
		Account saving = accountRepository.findById(savingId).orElseThrow(() -> new NoSuchElementException());

		if (mainAccount.getAccountBalance() - savingRequestDto.getAmount() < 0) {
			throw new HttpClientErrorException(HttpStatusCode.valueOf(400));
		}
		mainAccount.updateSaving(mainAccount.getAccountBalance() - savingRequestDto.getAmount());
		saving.updateSaving(saving.getAccountBalance() + savingRequestDto.getAmount());

		return RemittanceResponseDto.builder()
			.responseMsg(RemittanceResponseMsg.SUCCESS.getResponseMsg())
			.build();
	}
}
