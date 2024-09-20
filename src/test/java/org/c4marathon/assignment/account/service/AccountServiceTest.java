package org.c4marathon.assignment.account.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

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
import org.c4marathon.assignment.domain.account.repository.AccountRepository;
import org.c4marathon.assignment.domain.account.service.AccountService;
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
		given(accountRepository.save(any(Account.class))).willAnswer(invocation -> invocation.getArgument(0)); // 전달된 계좌 객체를 반환

		// when
		accountService.createMain(scheduleCreateEvent);

		// then
		then(accountRepository).should(times(1)).save(any(Account.class));
	}

	@DisplayName("메인 계좌충전")
	@Test
	void charMain(){
		//given
		Long userId = 1L;
		RemittanceRequestDto remittanceRequestDto = new RemittanceRequestDto(3288494829384L, 1000000L);

		given(httpServletRequest.getSession(false)).willReturn(httpSession);
		given(httpSession.getAttribute("userId")).willReturn(userId);
		given(accountRepository.findByAccountNum(anyLong())).willReturn(mainAccount);

		// when
		RemittanceResponseDto response = accountService.chargeMain(remittanceRequestDto, userId, httpServletRequest);

		// then
		assertThat(response.responseMsg()).isEqualTo(RemittanceResponseMsg.SUCCESS.getResponseMsg());
		verify(accountRepository).findByAccountNum(anyLong());
	}

	@DisplayName("세션정보가 없을 시 계좌 충전은 실패한다.")
	@Test
	void chargeMainNoSessionErr(){
		//given
		RemittanceRequestDto remittanceRequestDto = new RemittanceRequestDto(3288494829384L, 1000000L);

		given(httpServletRequest.getSession(false)).willReturn(null);
		// when // then
		assertThrows(UserException.class, () -> accountService.chargeMain(remittanceRequestDto, 1L, httpServletRequest));
	}

	@DisplayName("계좌 충전 시 세션에 등록된 유저와 요청한 유저가 다르다면 충전은 실패한다.")
	@Test
	void chargeMainInvalidSessionId(){
		// given
		RemittanceRequestDto remittanceRequestDto = new RemittanceRequestDto(3288494829384L, 1000000L);

		given(httpServletRequest.getSession(false)).willReturn(httpSession);
		given(httpSession.getAttribute("userId")).willReturn(2L); // 세션의 userId와 다름

		// when // then
		assertThrows(UserException.class, () -> accountService.chargeMain(remittanceRequestDto, user.getUserId(), httpServletRequest));
	}

	@DisplayName("계좌가 비활성화 상태이면 충전은 실패한다.")
	@Test
	void chargeMainAccountUnavailable(){
		//given
		Long userId = 1L;
		RemittanceRequestDto remittanceRequestDto = new RemittanceRequestDto(3288494829384L, 1000000L);
		mainAccount = Account.
			builder()
			.accountStatus(AccountStatus.UNAVAILABLE)
			.build();
		given(httpServletRequest.getSession(false)).willReturn(httpSession);
		given(httpSession.getAttribute("userId")).willReturn(userId);
		given(accountRepository.findByAccountNum(anyLong())).willReturn(mainAccount);

		// when // then
		assertThrows(AccountException.class, () -> accountService.chargeMain(remittanceRequestDto, userId, httpServletRequest));
	}

	@DisplayName("일일 한도 초과 시 충전은 실패한다.")
	@Test
	void chargeMainDailyChargeLimitErr(){
		Long userId = 1L;
		mainAccount = Account.builder()
			.dailyChargeLimit(3000000)
			.build();
		RemittanceRequestDto remittanceRequestDto = new RemittanceRequestDto(3288494829384L, 1000000L);

		given(httpServletRequest.getSession(false)).willReturn(httpSession);
		given(httpSession.getAttribute("userId")).willReturn(userId);
		given(accountRepository.findByAccountNum(anyLong())).willReturn(mainAccount);

		//when //then
		assertThrows(AccountException.class, () -> accountService.chargeMain(remittanceRequestDto, userId, httpServletRequest));
	}
	@DisplayName("송금액이 일일한도 초과 시 충전은 실패한다.")
	@Test
	void chargeMainRemittanceAmountErr(){
		assertThrows(AccountException.class, () -> new RemittanceRequestDto(3288494829384L, 3000001L));
	}

	@DisplayName("메인계좌 외 적금계좌생성")
	@Test
	void createAccountSavings(){
		// given
		Long userId = 1L;

		// 모킹: 세션에서 userId가 반환되도록 설정
		given(httpServletRequest.getSession(false)).willReturn(httpSession);
		given(httpSession.getAttribute("userId")).willReturn(userId);

		// 모킹: userRepository에서 userId로 조회된 User 객체를 반환
		given(userRepository.findById(userId)).willReturn(Optional.of(user));

		// 모킹: 계좌번호 중복 검사 및 계좌 저장 처리
		given(accountRepository.existsByAccountNum(anyLong())).willReturn(false); // 계좌 중복 검사에서 중복이 없다고 설정
		given(accountRepository.save(any(Account.class))).willAnswer(invocation -> invocation.getArgument(0)); // 전달된 계좌 객체를 반환

		// when
		accountService.createAccountOther(userId, "SAVINGS", httpServletRequest);

		// then
		then(accountRepository).should(times(1)).save(any(Account.class)); // 계좌 저장이 호출되었는지 검증
		then(userRepository).should(times(1)).findById(userId); // userRepository에서 사용자 조회가 호출되었는지 검증
	}
	@DisplayName("메인계좌 외 적금계좌생성")
	@Test
	void createAccountOther(){
		// given
		Long userId = 1L;

		// 모킹: 세션에서 userId가 반환되도록 설정
		given(httpServletRequest.getSession(false)).willReturn(httpSession);
		given(httpSession.getAttribute("userId")).willReturn(userId);

		// 모킹: userRepository에서 userId로 조회된 User 객체를 반환
		given(userRepository.findById(userId)).willReturn(Optional.of(user));

		// 모킹: 계좌번호 중복 검사 및 계좌 저장 처리
		given(accountRepository.existsByAccountNum(anyLong())).willReturn(false); // 계좌 중복 검사에서 중복이 없다고 설정
		given(accountRepository.save(any(Account.class))).willAnswer(invocation -> invocation.getArgument(0)); // 전달된 계좌 객체를 반환

		// when
		accountService.createAccountOther(userId, "OTHERS", httpServletRequest);

		// then
		then(accountRepository).should(times(1)).save(any(Account.class)); // 계좌 저장이 호출되었는지 검증
		then(userRepository).should(times(1)).findById(userId); // userRepository에서 사용자 조회가 호출되었는지 검증
	}
	@DisplayName("타입이 만약 의도하지 않은 타입으로 요청된다면 예외가 발생한다.")
	@Test
	void createAccountOtherErr(){
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
		assertThrows(AccountException.class, ()-> accountService.createAccountOther(userId, "울랄라", httpServletRequest));
	}
	@DisplayName("메인 계좌에서 적금 계좌로 송금 테스트")
	@Test
	void savingRemittance() {
		// given
		Long savingId = 1L;
		Long userId = 1L;  // userId를 세션에 저장할 값과 동일하게 설정

		int remittanceAmount = 100000;

		Account savingAccount = Account.builder()
			.accountNum(4288494829384L)
			.accountRole(AccountRole.SAVINGS)
			.accountBalance(0L)
			.accountStatus(AccountStatus.AVAILABLE)
			.dailyChargeLimit(0)
			.user(user)
			.build();

		SavingRequestDto savingRequestDto = new SavingRequestDto(remittanceAmount);

		// Mock 설정: 세션에 userId를 저장 (세션에 저장된 ID와 requestUserId를 동일하게 설정)
		given(httpServletRequest.getSession(false)).willReturn(httpSession);
		given(httpSession.getAttribute("userId")).willReturn(userId);  // 세션에서 userId 반환

		// Mock 설정: 사용자와 계좌 조회
		given(accountRepository.findUserByAccount(savingId)).willReturn(user);
		given(accountRepository.findMainAccount(user.getUserId(), AccountRole.MAIN)).willReturn(mainAccount);
		given(accountRepository.findById(savingId)).willReturn(Optional.of(savingAccount));

		// when
		RemittanceResponseDto response = accountService.savingRemittance(savingId, savingRequestDto, httpServletRequest);

		// then
		then(accountRepository).should(times(1)).findUserByAccount(savingId);
		then(accountRepository).should(times(1)).findMainAccount(user.getUserId(), AccountRole.MAIN);
		then(accountRepository).should(times(1)).findById(savingId);

		// 잔액 차감 및 입금 확인
		assertThat(mainAccount.getAccountBalance()).isEqualTo(5000000L - remittanceAmount);
		assertThat(savingAccount.getAccountBalance()).isEqualTo(remittanceAmount);

		assertThat(response.responseMsg()).isEqualTo(RemittanceResponseMsg.SUCCESS.getResponseMsg());
	}
	@DisplayName("잔액 부족으로 적금 송금 실패 테스트")
	@Test
	void savingRemittanceErr() {
		// given
		Long savingId = 1L;
		Long userId = 1L;
		int remittanceAmount = 6000000; // 송금하려는 금액이 잔액보다 큼

		Account savingAccount = Account.builder()
			.accountNum(4288494829384L)
			.accountRole(AccountRole.SAVINGS)
			.accountBalance(0L)
			.accountStatus(AccountStatus.AVAILABLE)
			.dailyChargeLimit(0)
			.user(user)
			.build();

		SavingRequestDto savingRequestDto = new SavingRequestDto(remittanceAmount);

		// Mock 설정: 세션에 userId를 저장
		given(httpServletRequest.getSession(false)).willReturn(httpSession);
		given(httpSession.getAttribute("userId")).willReturn(userId);

		// Mock 설정: 사용자와 계좌 조회
		given(accountRepository.findUserByAccount(savingId)).willReturn(user);
		given(accountRepository.findMainAccount(user.getUserId(), AccountRole.MAIN)).willReturn(mainAccount);
		given(accountRepository.findById(savingId)).willReturn(Optional.of(savingAccount));

		// when & then
		AccountException thrownException = assertThrows(AccountException.class,
			() -> accountService.savingRemittance(savingId, savingRequestDto, httpServletRequest));

		// 예외 메시지 확인
		assertThat(thrownException.getAccountErrCode()).isEqualTo(AccountErrCode.ACCOUNT_INSUFFICIENT_BALANCE);
	}

}
