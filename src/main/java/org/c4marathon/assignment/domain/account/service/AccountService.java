package org.c4marathon.assignment.domain.account.service;

import static org.c4marathon.assignment.domain.user.entity.UserErrCode.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

import org.c4marathon.assignment.domain.account.dto.CreateResponseDto;
import org.c4marathon.assignment.domain.account.dto.RemittanceRequestDto;
import org.c4marathon.assignment.domain.account.dto.RemittanceResponseDto;
import org.c4marathon.assignment.domain.account.dto.SavingRequestDto;
import org.c4marathon.assignment.domain.account.entity.Account;
import org.c4marathon.assignment.domain.account.entity.AccountErrCode;
import org.c4marathon.assignment.domain.account.entity.AccountRole;
import org.c4marathon.assignment.domain.account.entity.AccountStatus;
import org.c4marathon.assignment.domain.account.entity.CreateResponseMsg;
import org.c4marathon.assignment.domain.account.entity.RemittanceResponseMsg;
import org.c4marathon.assignment.domain.account.entity.ScheduleCreateEvent;
import org.c4marathon.assignment.domain.account.exception.AccountException;
import org.c4marathon.assignment.domain.account.repository.AccountRepository;
import org.c4marathon.assignment.domain.user.entity.User;
import org.c4marathon.assignment.domain.user.exception.UserException;
import org.c4marathon.assignment.domain.user.repository.UserRepository;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {
	private final AccountRepository accountRepository;
	private final UserRepository userRepository;

	//메인계좌 생성
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void createMain(ScheduleCreateEvent scheduleCreateEvent) {
		User user = scheduleCreateEvent.user();
		Long accountNum = createRandomAccount();
		Account account = createAccount(user, accountNum, AccountRole.MAIN);
		accountRepository.save(account);
	}

	//계좌번호 생성
	private Long createRandomAccount() {
		long min = 300_0000_0000_00L;
		long max = 399_9999_9999_99L;
		Random random = new Random();
		Long accountNum = min + (long)(random.nextDouble() * (max - min + 1));

		if (duplicatedAccount(accountNum)) {
			return createRandomAccount();
		}
		return accountNum;
	}

	//계좌 Entity 생성
	private Account createAccount(User user, Long accountNum, AccountRole accountRole) {
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
	private boolean duplicatedAccount(Long accountNum) {
		return accountRepository.existsByAccountNum(accountNum);
	}

	//메인계좌 충전
	@Transactional(isolation = Isolation.REPEATABLE_READ)
	public RemittanceResponseDto chargeMain(RemittanceRequestDto remittanceRequestDto, Long userId,
		HttpServletRequest httpServletRequest) {
		Long sessionId = getSessionId(httpServletRequest);
		validateUser(sessionId, userId);

		Long accountNum = remittanceRequestDto.accountNum();
		Account account = accountRepository.findByAccountNum(accountNum);
		Long remittanceAmount = remittanceRequestDto.remittanceAmount();
		validateCharge(account, remittanceAmount);
		account.updateChargeAccount(remittanceAmount);
		return new RemittanceResponseDto(RemittanceResponseMsg.SUCCESS.getResponseMsg());
	}

	private Long getSessionId(HttpServletRequest httpServletRequest) {
		HttpSession session = httpServletRequest.getSession(false);
		if (session == null || session.getAttribute("userId") == null) {
			throw new UserException(USER_SESSION_ERR);
		}
		return (Long)session.getAttribute("userId");
	}

	private void validateUser(Long sessionId, Long requestUserId) {
		if (!sessionId.equals(requestUserId)) {
			throw new UserException(USER_SESSION_ERR);
		}
	}

	//한도 및 계좌 상태 검사
	private void validateCharge(Account account, Long remittanceAmount) {
		if (account.getDailyChargeLimit() >= 3_000_000) {
			throw new AccountException(AccountErrCode.ACCOUNT_DALIYCHARGELIMIT_ERR);
		} else if (account.getAccountStatus() == AccountStatus.UNAVAILABLE) {
			throw new AccountException(AccountErrCode.ACCOUNT_UNAVAILABLE);
		} else if (account.getDailyChargeLimit() + remittanceAmount > 3_000_000) {
			throw new AccountException(AccountErrCode.ACCOUNT_DALIYCHARGELIMIT_ERR);
		}
	}

	//메인 외 계좌 생성
	public CreateResponseDto createAccountOther(Long userId, String createAccountRole, HttpServletRequest httpServletRequest) {
		Long sessionId = getSessionId(httpServletRequest);
		validateUser(sessionId, userId);

		User user = userRepository.findById(userId).orElseThrow(NoSuchElementException::new);
		Long accountNum = createRandomAccount();
		AccountRole accountRole = determineAccountRole(createAccountRole);
		if (duplicatedAccount(accountNum)) {
			throw new AccountException(AccountErrCode.ACCOUNT_CREATE_FAIL);
		}
		Account account = createAccount(user, accountNum, accountRole);
		accountRepository.save(account);
		return new CreateResponseDto(CreateResponseMsg.SUCCESS.getResponseMsg());
	}

	//계좌 역할 구별
	private AccountRole determineAccountRole(String createAccountRole) {
		return switch (createAccountRole) {
			case "SAVINGS" -> AccountRole.SAVINGS;
			case "OTHERS" -> AccountRole.OTHERS;
			default -> throw new AccountException(AccountErrCode.INVALID_ACCOUNT_TYPE);
		};
	}

	//메인계좌에서 인출 후 적금계좌에 입금
	@Transactional(isolation = Isolation.REPEATABLE_READ)
	public RemittanceResponseDto savingRemittance(Long savingId, SavingRequestDto savingRequestDto, HttpServletRequest httpServletRequest) {
		Long sessionId = getSessionId(httpServletRequest);
		validateUser(sessionId, savingId);

		User user = accountRepository.findUserByAccount(savingId);
		Account mainAccount = accountRepository.findMainAccount(user.getUserId(), AccountRole.MAIN);
		Account saving = accountRepository.findById(savingId).orElseThrow(NoSuchElementException::new);
		if (mainAccount.getAccountBalance() - savingRequestDto.amount() < 0) {
			throw new AccountException(AccountErrCode.ACCOUNT_INSUFFICIENT_BALANCE);
		}
		mainAccount.updateSaving(mainAccount.getAccountBalance() - savingRequestDto.amount());
		saving.updateSaving(saving.getAccountBalance() + savingRequestDto.amount());
		return new RemittanceResponseDto(RemittanceResponseMsg.SUCCESS.getResponseMsg());
	}

	@Scheduled(cron = "0 0 0 * * ?")
	@Transactional
	public void resetDailyChargeLimit() {
		List<Account> accountList = accountRepository.findAll();
		for (Account account : accountList
		) {
			account.updateDailyChargeLimit(0);
		}
	}
}
