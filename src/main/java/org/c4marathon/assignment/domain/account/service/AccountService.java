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
	private static final long MIN_ACCOUNT_NUM = 300_0000_0000_00L;
	private static final long MAX_ACCOUNT_NUM = 399_9999_9999_99L;
	private static final long MAX_DAILY_CHARGE_LIMIT = 3_000_000L;
	private static final int BALANCE_UNIT = 10000;

	private final AccountRepository accountRepository;
	private final UserRepository userRepository;

	//메인계좌 생성
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void createMain(ScheduleCreateEvent scheduleCreateEvent) {
		User user = scheduleCreateEvent.user();
		Long accountNum = getAccountNum();
		Account account = createAccount(user, accountNum, AccountRole.MAIN);
		accountRepository.save(account);
	}

	//계좌번호 생성
	private Long getAccountNum() {
		Long accountNum;
		do {
			accountNum = createRandomAccount();
		} while (duplicatedAccount(accountNum));
		return accountNum;
	}

	private Long createRandomAccount() {
		Random random = new Random();
		return MIN_ACCOUNT_NUM + (long)(random.nextDouble() * (MAX_ACCOUNT_NUM - MIN_ACCOUNT_NUM + 1));
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
	public RemittanceResponseDto chargeMain(RemittanceRequestDto remittanceRequestDto) {
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

	//한도 및 계좌 상태 검사
	private void validateCharge(Account account, Long remittanceAmount) {
		if (account.getDailyChargeLimit() >= MAX_DAILY_CHARGE_LIMIT) {
			throw new AccountException(AccountErrCode.ACCOUNT_DALIYCHARGELIMIT_ERR);
		} else if (account.getAccountStatus() == AccountStatus.UNAVAILABLE) {
			throw new AccountException(AccountErrCode.ACCOUNT_UNAVAILABLE);
		} else if (account.getDailyChargeLimit() + remittanceAmount > MAX_DAILY_CHARGE_LIMIT) {
			throw new AccountException(AccountErrCode.ACCOUNT_DALIYCHARGELIMIT_ERR);
		}
	}

	//메인 외 계좌 생성
	public CreateResponseDto createAccountOther(String createAccountRole, HttpServletRequest httpServletRequest) {
		Long sessionId = getSessionId(httpServletRequest);
		User user = userRepository.findById(sessionId).orElseThrow(NoSuchElementException::new);
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
		Long userId = getSessionId(httpServletRequest);
		User user = userRepository.findById(userId).orElseThrow(()->new UserException(USER_NOT_FOUND));
		Account mainAccount = accountRepository.findMainAccount(user.getUserId(), AccountRole.MAIN);
		Account saving = accountRepository.findById(savingId).orElseThrow(NoSuchElementException::new);
		if (mainAccount.getAccountBalance() - savingRequestDto.amount() < 0) {
			long chargeBalance;
			chargeBalance = calculateChargeBalance(mainAccount.getAccountBalance(), (long)savingRequestDto.amount());
			RemittanceRequestDto chargeRemittanceDto = new RemittanceRequestDto(mainAccount.getAccountNum(), chargeBalance);
			chargeMain(chargeRemittanceDto);
		}
		mainAccount.updateSaving(mainAccount.getAccountBalance() - savingRequestDto.amount());
		saving.updateSaving(saving.getAccountBalance() + savingRequestDto.amount());
		return new RemittanceResponseDto(RemittanceResponseMsg.SUCCESS.getResponseMsg());
	}

	//메인계좌 간의 송금
	@Transactional(isolation = Isolation.REPEATABLE_READ, timeout = 60)
	public RemittanceResponseDto remittanceOtherMain(RemittanceRequestDto remittanceRequestDto, HttpServletRequest httpServletRequest){
		Long userId = getSessionId(httpServletRequest);
		Account mainAccount = accountRepository.findMainAccount(userId, AccountRole.MAIN);

		Long receiveAccountNum = remittanceRequestDto.accountNum();
		Account receiveAccount = accountRepository.findByAccountNum(receiveAccountNum);
		Long remittanceAmount = remittanceRequestDto.remittanceAmount();
		validateCharge(receiveAccount, remittanceAmount);
		if (mainAccount.getAccountBalance() - remittanceAmount < 0) {
			long chargeBalance;
			chargeBalance = calculateChargeBalance(mainAccount.getAccountBalance(), remittanceAmount);
			RemittanceRequestDto chargeRemittanceDto = new RemittanceRequestDto(mainAccount.getAccountNum(), chargeBalance);
			chargeMain(chargeRemittanceDto );
		}
		mainAccount.updateSaving(mainAccount.getAccountBalance() - remittanceAmount);
		receiveAccount.updateSaving(remittanceAmount);
		return new RemittanceResponseDto(RemittanceResponseMsg.SUCCESS.getResponseMsg());
	}

	private Long calculateChargeBalance(Long balance, Long remittanceAmount){
		return ((long)Math.ceil((double)Math.abs(balance - remittanceAmount) / BALANCE_UNIT)) * BALANCE_UNIT;
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
