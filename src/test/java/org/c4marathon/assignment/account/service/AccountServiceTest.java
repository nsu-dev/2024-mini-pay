package org.c4marathon.assignment.account.service;

import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.c4marathon.assignment.domain.account.dto.AccountErrDto;
import org.c4marathon.assignment.domain.account.dto.RemittanceRequestDto;
import org.c4marathon.assignment.domain.account.dto.RemittanceResponseDto;
import org.c4marathon.assignment.domain.account.dto.SavingRequestDto;
import org.c4marathon.assignment.domain.account.entity.Account;
import org.c4marathon.assignment.domain.account.entity.AccountErrCode;
import org.c4marathon.assignment.domain.account.entity.AccountRole;
import org.c4marathon.assignment.domain.account.entity.AccountStatus;
import org.c4marathon.assignment.domain.account.entity.RemittanceResponseMsg;
import org.c4marathon.assignment.domain.account.entity.ScheduleCreateEvent;
import org.c4marathon.assignment.domain.account.exception.AccountException;
import org.c4marathon.assignment.domain.account.exception.AccountExceptionHandler;
import org.c4marathon.assignment.domain.account.repository.AccountRepository;
import org.c4marathon.assignment.domain.account.service.AccountService;
import org.c4marathon.assignment.domain.account.transaction.TransactionHandler;
import org.c4marathon.assignment.domain.user.entity.User;
import org.c4marathon.assignment.domain.user.exception.UserException;
import org.c4marathon.assignment.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

	@Mock
	private AccountRepository accountRepository;

	@InjectMocks
	private AccountService accountService;
	@Mock
	private HttpServletRequest httpServletRequest;

	@Mock
	private HttpSession httpSession;

	@Mock
	private UserRepository userRepository;
	@Mock
	private TransactionHandler transactionHandler;
	@Mock
	private AccountErrCode mockUserErrCode;
	@InjectMocks
	private AccountExceptionHandler accountExceptionHandler;
	private User user;
	private Account mainAccount;

	@BeforeEach
	void setUp() {
		user = User.builder()
			.userId(1L)
			.userPhone("010-8337-6023")
			.userName("조아빈")
			.userBirth("20000604")
			.userPassword("pw123")
			.build();
		mainAccount = Account.builder()
			.accountNum(3288494829384L)
			.accountRole(AccountRole.MAIN)
			.accountBalance(5000000L)
			.accountStatus(AccountStatus.AVAILABLE)
			.dailyChargeLimit(0)
			.user(user)
			.build();
	}

	@DisplayName("메인 계좌가 정상적으로 생성되고 저장되는지 검증")
	@Test
	void createMain() {
		// given
		ScheduleCreateEvent scheduleCreateEvent = new ScheduleCreateEvent(user);
		given(accountRepository.existsByAccountNum(anyLong())).willReturn(false);
		given(accountRepository.save(any(Account.class))).willAnswer(
			invocation -> invocation.getArgument(0)); // 전달된 계좌 객체를 반환

		// when
		accountService.createMain(scheduleCreateEvent);

		// then
		then(accountRepository).should(times(1)).save(any(Account.class));
	}

	@DisplayName("메인 계좌충전")
	@Test
	void charMain() {
		//givens
		RemittanceRequestDto remittanceRequestDto = new RemittanceRequestDto(3288494829384L, 1000000L);
		// Mock repository to return the test account
		given(accountRepository.findByAccountNum(remittanceRequestDto.accountNum())).willReturn(mainAccount);
		doAnswer(invocation -> {
			// 실제로 수행할 동작 정의
			((TransactionHandler.Action)invocation.getArgument(0)).act();
			return null;
		}).when(transactionHandler).runInRepeatableTransaction(any());
		// Run the chargeMain method
		RemittanceResponseDto response = accountService.chargeMain(remittanceRequestDto);

		// Verify transaction and balance update
		verify(transactionHandler, times(1)).runInRepeatableTransaction(any());
		verify(accountRepository, times(1)).findByAccountNum(mainAccount.getAccountNum());

		assertEquals(response.responseMsg(), RemittanceResponseMsg.SUCCESS.getResponseMsg());
		assertEquals(6000000L, mainAccount.getAccountBalance());
	}

	@DisplayName("계좌가 비활성화 상태이면 충전은 실패한다.")
	@Test
	void chargeMainAccountUnavailable() {
		//given
		RemittanceRequestDto remittanceRequestDto = new RemittanceRequestDto(3288494829384L, 1000000L);
		doAnswer(invocation -> {
			// 실제로 수행할 동작 정의
			((TransactionHandler.Action)invocation.getArgument(0)).act();
			return null;
		}).when(transactionHandler).runInRepeatableTransaction(any());

		mainAccount = Account.
			builder()
			.accountStatus(AccountStatus.UNAVAILABLE)
			.build();
		given(accountRepository.findByAccountNum(anyLong())).willReturn(mainAccount);
		// when // then
		assertThrows(AccountException.class, () -> accountService.chargeMain(remittanceRequestDto));
	}

	@DisplayName("일일 한도 초과 시 충전은 실패한다.")
	@Test
	void chargeMainDailyChargeLimitErr() {
		mainAccount = Account.builder()
			.dailyChargeLimit(3000000)
			.build();
		RemittanceRequestDto remittanceRequestDto = new RemittanceRequestDto(3288494829384L, 1000000L);
		given(accountRepository.findByAccountNum(anyLong())).willReturn(mainAccount);
		doAnswer(invocation -> {
			// 실제로 수행할 동작 정의
			((TransactionHandler.Action)invocation.getArgument(0)).act();
			return null;
		}).when(transactionHandler).runInRepeatableTransaction(any());
		//when //then
		assertThrows(AccountException.class, () -> accountService.chargeMain(remittanceRequestDto));
	}

	@DisplayName("송금액과 일일한도의 합이 한도초과 시 예외가 발생한다.")
	@Test
	void chargeValidateChargeErr() {
		mainAccount = Account.builder()
			.dailyChargeLimit(2500000)
			.build();
		RemittanceRequestDto remittanceRequestDto = new RemittanceRequestDto(3288494829384L, 1000000L);
		given(accountRepository.findByAccountNum(anyLong())).willReturn(mainAccount);
		doAnswer(invocation -> {
			// 실제로 수행할 동작 정의
			((TransactionHandler.Action)invocation.getArgument(0)).act();
			return null;
		}).when(transactionHandler).runInRepeatableTransaction(any());
		//when //then
		assertThrows(AccountException.class, () -> accountService.chargeMain(remittanceRequestDto));
	}

	@DisplayName("송금액이 일일한도 초과 시 충전은 실패한다.")
	@Test
	void chargeMainRemittanceAmountErr() {
		assertThrows(AccountException.class, () -> new RemittanceRequestDto(3288494829384L, 3000001L));
	}

	@DisplayName("메인계좌 외 적금계좌생성")
	@Test
	void createAccountSavings() {
		// given
		Long userId = 1L;
		user = User.builder()
			.userId(1L)
			.userPhone("010-8337-6023")
			.userName("조아빈")
			.userBirth("20000604")
			.userPassword("pw123")
			.build();
		// 모킹: 세션에서 userId가 반환되도록 설정
		given(httpServletRequest.getSession(false)).willReturn(httpSession);
		given(httpSession.getAttribute("userId")).willReturn(userId);

		// 모킹: userRepository에서 userId로 조회된 User 객체를 반환
		given(userRepository.findById(userId)).willReturn(Optional.of(user));

		// 모킹: 계좌번호 중복 검사 및 계좌 저장 처리
		given(accountRepository.existsByAccountNum(anyLong())).willReturn(false); // 계좌 중복 검사에서 중복이 없다고 설정
		given(accountRepository.save(any(Account.class))).willAnswer(
			invocation -> invocation.getArgument(0)); // 전달된 계좌 객체를 반환

		// when
		accountService.createAccountOther("SAVINGS", httpServletRequest);

		// then
		then(accountRepository).should(times(1)).save(any(Account.class)); // 계좌 저장이 호출되었는지 검증
		then(userRepository).should(times(1)).findById(userId); // userRepository에서 사용자 조회가 호출되었는지 검증
	}

	@DisplayName("적금계좌생성시 세션에 저장한 userId가 없는 경우 예외가 발생한다.")
	@Test
	void createAccountSavingsNotSessionId() {
		// given
		Long userId = 1L;
		user = User.builder()
			.userId(1L)
			.userPhone("010-8337-6023")
			.userName("조아빈")
			.userBirth("20000604")
			.userPassword("pw123")
			.build();
		// 모킹: 세션에서 userId가 반환되도록 설정
		given(httpServletRequest.getSession(false)).willReturn(httpSession);
		// when
		// then
		assertThrows(UserException.class, () -> accountService.createAccountOther("SAVINGS", httpServletRequest));
	}

	@DisplayName("적금계좌생성시 세션에 저장한 세션 자체가 없는 경우 예외가 발생한다.")
	@Test
	void createAccountSavingsNotSession() {
		// given
		user = User.builder()
			.userId(1L)
			.userPhone("010-8337-6023")
			.userName("조아빈")
			.userBirth("20000604")
			.userPassword("pw123")
			.build();

		// when
		// then
		assertThrows(UserException.class, () -> accountService.createAccountOther("SAVINGS", httpServletRequest));
	}

	@DisplayName("메인계좌 외 계좌생성")
	@Test
	void createAccountOther() {
		// given
		Long userId = 1L;

		// 모킹: 세션에서 userId가 반환되도록 설정
		given(httpServletRequest.getSession(false)).willReturn(httpSession);
		given(httpSession.getAttribute("userId")).willReturn(userId);

		// 모킹: userRepository에서 userId로 조회된 User 객체를 반환
		given(userRepository.findById(userId)).willReturn(Optional.of(user));

		// 모킹: 계좌번호 중복 검사 및 계좌 저장 처리
		given(accountRepository.existsByAccountNum(anyLong())).willReturn(false); // 계좌 중복 검사에서 중복이 없다고 설정
		given(accountRepository.save(any(Account.class))).willAnswer(
			invocation -> invocation.getArgument(0)); // 전달된 계좌 객체를 반환

		// when
		accountService.createAccountOther("OTHERS", httpServletRequest);

		// then
		then(accountRepository).should(times(1)).save(any(Account.class)); // 계좌 저장이 호출되었는지 검증
		then(userRepository).should(times(1)).findById(userId); // userRepository에서 사용자 조회가 호출되었는지 검증
	}

	@DisplayName("타입이 만약 의도하지 않은 타입으로 요청된다면 예외가 발생한다.")
	@Test
	void createAccountOtherErr() {
		// given
		Long userId = 1L;

		// 모킹: 세션에서 userId가 반환되도록 설정
		given(httpServletRequest.getSession(false)).willReturn(httpSession);
		given(httpSession.getAttribute("userId")).willReturn(userId);

		// 모킹: userRepository에서 userId로 조회된 User 객체를 반환
		given(userRepository.findById(userId)).willReturn(Optional.of(user));

		// 모킹: 계좌번호 중복 검사 및 계좌 저장 처리
		given(accountRepository.existsByAccountNum(anyLong())).willReturn(false); // 계좌 중복 검사에서 중복이 없다고 설정

		// when
		assertThrows(AccountException.class, () -> accountService.createAccountOther("울랄라", httpServletRequest));
	}

	@DisplayName("메인 계좌에서 적금 계좌로 송금 테스트")
	@Test
	void savingRemittance() {
		// given
		Long savingId = 1L;
		Long userId = 1L;
		int remittanceAmount = 100000;  // 송금 금액

		// 적금 계좌 및 메인 계좌 설정
		Account savingAccount = Account.builder()
			.accountNum(4288494829384L)
			.accountRole(AccountRole.SAVINGS)
			.accountBalance(0L)
			.accountStatus(AccountStatus.AVAILABLE)
			.dailyChargeLimit(0)
			.user(user)
			.build();

		SavingRequestDto savingRequestDto = new SavingRequestDto(remittanceAmount);

		// Mock 설정: 세션에서 userId 반환
		given(httpServletRequest.getSession(false)).willReturn(httpSession);
		given(httpSession.getAttribute("userId")).willReturn(userId);

		// Mock 설정: 사용자와 계좌 조회
		given(userRepository.findById(userId)).willReturn(Optional.of(user)); // user 조회
		given(accountRepository.findMainAccount(user.getUserId(), AccountRole.MAIN)).willReturn(
			mainAccount); // 메인 계좌 조회
		given(accountRepository.findById(savingId)).willReturn(Optional.of(savingAccount)); // 적금 계좌 조회
		doAnswer(invocation -> {
			// 실제로 수행할 동작 정의
			((TransactionHandler.Action)invocation.getArgument(0)).act();
			return null;
		}).when(transactionHandler).runInCommittedTransaction(any());
		// when
		RemittanceResponseDto response = accountService.savingRemittance(savingId, savingRequestDto,
			httpServletRequest);

		// then
		// 메인 계좌에서 송금한 금액만큼 차감되었는지 확인
		assertThat(mainAccount.getAccountBalance()).isEqualTo(5000000L - remittanceAmount);

		// 적금 계좌에 금액이 입금되었는지 확인
		assertThat(savingAccount.getAccountBalance()).isEqualTo(remittanceAmount);

		// 송금 성공 메시지 확인
		assertThat(response.responseMsg()).isEqualTo(RemittanceResponseMsg.SUCCESS.getResponseMsg());

		// Mock 메서드 호출 검증
		then(accountRepository).should(times(1)).findMainAccount(user.getUserId(), AccountRole.MAIN);
		then(accountRepository).should(times(1)).findById(savingId);
	}

	@DisplayName("메인 계좌에서 적금 계좌로 송금 시 잔액 부족할 경우 충전 후 송금 처리 테스트")
	@Test
	void savingRemittanceWithInsufficientBalance() {
		// given
		Long savingId = 1L;
		Long userId = 1L;
		int remittanceAmount = 23000;  // 송금 금액
		Long initialMainAccountBalance = 5000L;  // 메인 계좌의 초기 잔액 (부족함)

		// 예상 충전 금액: 10000원 단위로 충전 (23000원 - 5000원 = 18000원 -> 20000원 충전 필요)
		Long expectedChargeAmount = 20000L;  // 충전해야 할 금액

		// 메인 계좌 설정 (초기 잔액이 송금 금액보다 적음)
		mainAccount = Account.builder().accountBalance(initialMainAccountBalance).build();

		// 적금 계좌 설정
		Account savingAccount = Account.builder()
			.accountNum(4288494829384L)
			.accountRole(AccountRole.SAVINGS)
			.accountBalance(0L)
			.accountStatus(AccountStatus.AVAILABLE)
			.dailyChargeLimit(0)
			.user(user)
			.build();

		SavingRequestDto savingRequestDto = new SavingRequestDto(remittanceAmount);

		// Mock 설정: 세션에서 userId를 반환하도록 설정
		given(httpServletRequest.getSession(false)).willReturn(httpSession);
		given(httpSession.getAttribute("userId")).willReturn(userId);

		// Mock 설정: 사용자 및 계좌 조회
		given(userRepository.findById(userId)).willReturn(Optional.of(user));  // user 조회
		given(accountRepository.findMainAccount(user.getUserId(), AccountRole.MAIN)).willReturn(
			mainAccount);  // 메인 계좌 조회
		given(accountRepository.findById(savingId)).willReturn(Optional.of(savingAccount));  // 적금 계좌 조회
		// Mock 설정: 충전 로직에서 충전 금액 처리
		given(accountRepository.findByAccountNum(mainAccount.getAccountNum())).willReturn(mainAccount);
		doAnswer(invocation -> {
			// 실제로 수행할 동작 정의
			((TransactionHandler.Action)invocation.getArgument(0)).act();
			return null;
		}).when(transactionHandler).runInCommittedTransaction(any());

		// when
		RemittanceResponseDto response = accountService.savingRemittance(savingId, savingRequestDto,
			httpServletRequest);

		// then
		// 메인 계좌의 잔액이 충전 후 송금되었는지 확인
		Long newMainAccountBalance = initialMainAccountBalance + expectedChargeAmount - remittanceAmount;
		assertThat(mainAccount.getAccountBalance()).isEqualTo(newMainAccountBalance);

		// 적금 계좌에 금액이 입금되었는지 확인
		assertThat(savingAccount.getAccountBalance()).isEqualTo(remittanceAmount);

		// 송금 성공 메시지 확인
		assertThat(response.responseMsg()).isEqualTo(RemittanceResponseMsg.SUCCESS.getResponseMsg());

		// Mock 메서드 호출 검증
		then(accountRepository).should(times(1)).findByAccountNum(mainAccount.getAccountNum());
		then(accountRepository).should(times(1)).findMainAccount(user.getUserId(), AccountRole.MAIN);
		then(accountRepository).should(times(1)).findById(savingId);
	}

	@DisplayName("메인계좌간의 거래")
	@Test
	void remittanceMainOther() throws InterruptedException {
		// given
		Long userId = 1L;
		Long remittanceAmount = 100000L;
		Long receiveAccountBalance = 200000L;

		// 송금 요청 DTO 설정
		RemittanceRequestDto remittanceRequestDto = new RemittanceRequestDto(3288494829385L,
			remittanceAmount); // 수신 계좌 번호와 송금 금액

		// 수신 메인 계좌 설정
		Account receiveAccount = Account.builder()
			.accountNum(3288494829385L)
			.accountRole(AccountRole.MAIN)
			.accountBalance(receiveAccountBalance)
			.accountStatus(AccountStatus.AVAILABLE)
			.dailyChargeLimit(0)
			.build();

		// Mock 설정: 세션에서 userId 반환
		given(httpServletRequest.getSession(false)).willReturn(httpSession);
		given(httpSession.getAttribute("userId")).willReturn(userId);

		// Mock 설정: 메인 계좌 조회 및 수신 계좌 조회
		given(accountRepository.findMainAccount(userId, AccountRole.MAIN)).willReturn(mainAccount); // 송금하는 메인 계좌 조회
		given(accountRepository.findByAccountNum(remittanceRequestDto.accountNum())).willReturn(
			receiveAccount); // 수신 메인 계좌 조회
		doAnswer(invocation -> {
			// 실제로 수행할 동작 정의
			((TransactionHandler.Action)invocation.getArgument(0)).act();
			return null;
		}).when(transactionHandler).runInRepeatableTransaction(any());
		// when
		RemittanceResponseDto response = accountService.remittanceOtherMain(remittanceRequestDto, httpServletRequest);

		// then
		// 송금 후 메인 계좌 잔액 확인
		assertThat(mainAccount.getAccountBalance()).isEqualTo(5000000L - remittanceAmount);

		// 수신 계좌에 송금 금액이 입금되었는지 확인
		assertThat(receiveAccount.getAccountBalance()).isEqualTo(receiveAccountBalance + remittanceAmount);

		// 송금 성공 메시지 확인
		assertThat(response.responseMsg()).isEqualTo(RemittanceResponseMsg.SUCCESS.getResponseMsg());

		// Mock 메서드 호출 확인
		then(accountRepository).should(times(1)).findMainAccount(userId, AccountRole.MAIN);
		then(accountRepository).should(times(1)).findByAccountNum(remittanceRequestDto.accountNum());
	}

	@DisplayName("메인계좌간의 거래에서 잔액이 부족한 경우 잔액 충전 후 다시 진행")
	@Test
	void remittanceMainOtherWithInsufficientBalance() {
		// given
		Long userId = 1L;
		Long remittanceAmount = 100000L;
		Long receiveAccountBalance = 200000L;
		Long initialMainAccountBalance = 88000L;
		Long expectedChargeAmount = 20000L; // 충전해야할 금액
		// 송금 요청 DTO 설정
		RemittanceRequestDto remittanceRequestDto = new RemittanceRequestDto(3288494829385L,
			remittanceAmount); // 수신 계좌 번호와 송금 금액

		mainAccount = Account.builder().accountBalance(initialMainAccountBalance).build();

		// 수신 메인 계좌 설정
		Account receiveAccount = Account.builder()
			.accountNum(3288494829385L)
			.accountRole(AccountRole.MAIN)
			.accountBalance(receiveAccountBalance)
			.accountStatus(AccountStatus.AVAILABLE)
			.dailyChargeLimit(0)
			.build();

		// Mock 설정: 세션에서 userId 반환
		given(httpServletRequest.getSession(false)).willReturn(httpSession);
		given(httpSession.getAttribute("userId")).willReturn(userId);

		// Mock 설정: 메인 계좌 및 수신 계좌 조회
		given(accountRepository.findMainAccount(userId, AccountRole.MAIN)).willReturn(mainAccount);  // 송금하는 메인 계좌 조회
		given(accountRepository.findByAccountNum(remittanceRequestDto.accountNum())).willReturn(
			receiveAccount);  // 수신 계좌 조회

		// 충전 로직 Mock 설정
		given(accountRepository.findByAccountNum(mainAccount.getAccountNum())).willReturn(mainAccount);  // 충전 시 계좌 조회
		doAnswer(invocation -> {
			// 실제로 수행할 동작 정의
			((TransactionHandler.Action)invocation.getArgument(0)).act();
			return null;
		}).when(transactionHandler).runInRepeatableTransaction(any());
		// when
		RemittanceResponseDto response = accountService.remittanceOtherMain(remittanceRequestDto, httpServletRequest);

		// then
		// 메인 계좌의 잔액이 충전 후 송금되었는지 확인
		Long newMainAccountBalance = initialMainAccountBalance + expectedChargeAmount - remittanceAmount;
		assertThat(mainAccount.getAccountBalance()).isEqualTo(newMainAccountBalance);

		// 수신 계좌에 송금 금액이 입금되었는지 확인
		assertThat(receiveAccount.getAccountBalance()).isEqualTo(receiveAccountBalance + remittanceAmount);

		// 송금 성공 메시지 확인
		assertThat(response.responseMsg()).isEqualTo(RemittanceResponseMsg.SUCCESS.getResponseMsg());

		// Mock 메서드 호출 확인
		then(accountRepository).should(times(1)).findMainAccount(userId, AccountRole.MAIN);
		then(accountRepository).should(times(1)).findByAccountNum(remittanceRequestDto.accountNum());
		then(accountRepository).should(times(1)).findByAccountNum(mainAccount.getAccountNum());  // 충전 시 호출 여부 확인
	}

	@DisplayName("메인계좌간 거래에서 수신받는 계좌가 메인계좌가 아닌 경우 예외가 발생한다.")
	@Test
	void remittanceMainOtherErr() {
		// given
		Long userId = 1L;
		Long remittanceAmount = 100000L;
		Long receiveAccountBalance = 200000L;
		Long initialMainAccountBalance = 88000L;
		Long expectedChargeAmount = 20000L; // 충전해야할 금액
		// 송금 요청 DTO 설정
		RemittanceRequestDto remittanceRequestDto = new RemittanceRequestDto(3288494829385L,
			remittanceAmount); // 수신 계좌 번호와 송금 금액

		mainAccount = Account.builder().accountBalance(initialMainAccountBalance).build();

		// 수신 계좌를 적금계좌로 설정
		Account receiveAccount = Account.builder()
			.accountNum(3288494829385L)
			.accountRole(AccountRole.SAVINGS)
			.accountBalance(receiveAccountBalance)
			.accountStatus(AccountStatus.AVAILABLE)
			.dailyChargeLimit(0)
			.build();

		// Mock 설정: 세션에서 userId 반환
		given(httpServletRequest.getSession(false)).willReturn(httpSession);
		given(httpSession.getAttribute("userId")).willReturn(userId);

		// Mock 설정: 메인 계좌 및 수신 계좌 조회
		given(accountRepository.findMainAccount(userId, AccountRole.MAIN)).willReturn(mainAccount);  // 송금하는 메인 계좌 조회
		given(accountRepository.findByAccountNum(remittanceRequestDto.accountNum())).willReturn(
			receiveAccount);  // 수신 계좌 조회
		doAnswer(invocation -> {
			// 실제로 수행할 동작 정의
			((TransactionHandler.Action)invocation.getArgument(0)).act();
			return null;
		}).when(transactionHandler).runInRepeatableTransaction(any());
		// when //then
		assertThrows(AccountException.class,
			() -> accountService.remittanceOtherMain(remittanceRequestDto, httpServletRequest));
	}

	@Test
	@DisplayName("여러명의 사용자의 동시 송금 분산락 적용 없이 단순 트랜잭션만 있는 경우")
	void chargeMainConcurrencyTest() throws InterruptedException {
		// given
		int numberOfUsers = 10000;
		Account receiveAccount = Account.builder()
			.accountNum(3288494829385L)
			.accountRole(AccountRole.MAIN)
			.accountBalance(2000000L)
			.accountStatus(AccountStatus.AVAILABLE)
			.dailyChargeLimit(0)
			.build();
		ExecutorService executorService = Executors.newFixedThreadPool(numberOfUsers);
		CountDownLatch latch = new CountDownLatch(numberOfUsers); // 동시성을 제어하기 위한 래치
		long remittanceAmount = 100L; // 10,000원씩 송금
		Long initialMainBalance = mainAccount.getAccountBalance();
		Long initialReceiveBalance = receiveAccount.getAccountBalance();

		// 모킹: 세션에서 userId 반환
		given(httpServletRequest.getSession(false)).willReturn(httpSession);
		given(httpSession.getAttribute("userId")).willReturn(1L);

		// 모킹: 계좌 조회
		given(accountRepository.findMainAccount(anyLong(), eq(AccountRole.MAIN))).willReturn(mainAccount);
		given(accountRepository.findByAccountNum(eq(receiveAccount.getAccountNum()))).willReturn(receiveAccount);
		doAnswer(invocation -> {
			// 실제로 수행할 동작 정의
			((TransactionHandler.Action)invocation.getArgument(0)).act();
			return null;
		}).when(transactionHandler).runInRepeatableTransaction(any());
		// when: 100명의 사용자가 동시에 송금
		for (int i = 0; i < numberOfUsers; i++) {
			executorService.submit(() -> {
				try {
					RemittanceRequestDto remittanceRequestDto = new RemittanceRequestDto(receiveAccount.getAccountNum(),
						remittanceAmount);
					accountService.remittanceOtherMain(remittanceRequestDto, httpServletRequest);
				} catch (Exception e) {
					fail("Exception should not have occurred: ", e.getMessage());
				} finally {
					latch.countDown();
				}
			});
		}
		latch.await(); // 모든 스레드가 작업을 완료할 때까지 기다림
		executorService.shutdown();

		// then: 결과 검증
		// 메인 계좌 잔액이 100명에 의해 10,000원씩 빠졌으므로 예상되는 잔액
		Long expectedMainBalance = initialMainBalance - (remittanceAmount * numberOfUsers);
		// 수신 계좌는 100명으로부터 10,000원씩 받았으므로 예상되는 잔액
		Long expectedReceiveBalance = initialReceiveBalance + (remittanceAmount * numberOfUsers);

		assertThat(mainAccount.getAccountBalance()).isEqualTo(expectedMainBalance);
		assertThat(receiveAccount.getAccountBalance()).isEqualTo(expectedReceiveBalance);
	}

	@Test
	@DisplayName("모든 계좌의 한도가 초기화되는지 검증한다.")
	void resetDailyCharge() {
		Account account1 = mock(Account.class);
		Account account2 = mock(Account.class);

		// accountRepository.findAll() 메서드가 두 개의 Account를 반환하도록 모킹
		given(accountRepository.findAll()).willReturn(Arrays.asList(account1, account2));

		// 메서드 실행
		accountService.resetDailyChargeLimit();

		verify(account1, times(1)).updateDailyChargeLimit(0);
		verify(account2, times(1)).updateDailyChargeLimit(0);
	}

	@DisplayName("예외코드가 비정상적인 코드라도 예외를 정상적으로 처리한다.")
	@Test
	public void testHandleIllegalAccessError() {
		// UserErrCode.getStatus()가 잘못된 값을 반환하도록 설정하여 IllegalAccessError 발생 시뮬레이션
		given(mockUserErrCode.getStatus()).willReturn(9999); // 유효하지 않은 HTTP 상태 코드

		// getUserErrDto 호출 시 예외가 발생하는지 테스트
		ResponseEntity<AccountErrDto> response = accountExceptionHandler.getAccountErrDto(mockUserErrCode);

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
	}
}
