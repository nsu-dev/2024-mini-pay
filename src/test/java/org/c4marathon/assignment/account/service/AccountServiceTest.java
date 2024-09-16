package org.c4marathon.assignment.account.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.c4marathon.assignment.domain.account.dto.RemittanceRequestDto;
import org.c4marathon.assignment.domain.account.dto.RemittanceResponseDto;
import org.c4marathon.assignment.domain.account.entity.Account;
import org.c4marathon.assignment.domain.account.entity.AccountRole;
import org.c4marathon.assignment.domain.account.entity.AccountStatus;
import org.c4marathon.assignment.domain.account.entity.RemittanceResponseMsg;
import org.c4marathon.assignment.domain.account.entity.ScheduleCreateEvent;
import org.c4marathon.assignment.domain.account.exception.AccountException;
import org.c4marathon.assignment.domain.account.repository.AccountRepository;
import org.c4marathon.assignment.domain.account.service.AccountService;
import org.c4marathon.assignment.domain.user.entity.User;
import org.c4marathon.assignment.domain.user.exception.UserException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccountServiceTest {

	@Mock
	private AccountRepository accountRepository;

	@InjectMocks
	private AccountService accountService;

	@Mock
	private HttpServletRequest httpServletRequest;

	@Mock
	private HttpSession httpSession;

	private User user;
	private Account mainAccount;

	@BeforeEach
	void setUp() {
		user = User.builder().userId(1L).build();
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
		User user = User.builder()
			.userId(1L)
			.userPhone("010-8337-6023")
			.userName("조아빈")
			.userBirth("20000604")
			.userPassword("pw123")
			.build();

		ScheduleCreateEvent scheduleCreateEvent = new ScheduleCreateEvent(user);
		given(accountRepository.existsByAccountNum(anyLong())).willReturn(true);
		given(accountRepository.save(any(Account.class))).willAnswer(invocation -> invocation.getArgument(0)); // 전달된 계좌 객체를 반환

		// when
		accountService.createMain(scheduleCreateEvent); // 트리거

		// then
		then(accountRepository).should(times(1)).save(any(Account.class));
	}

	@DisplayName("메인 계좌충전")
	@Test
	void charMain(){
		//given
		Long userId = 1L;
		RemittanceRequestDto remittanceRequestDto = RemittanceRequestDto.builder()
			.accountNum(3288494829384L)
			.remittanceAmount(1000000L)
			.build();

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
		RemittanceRequestDto remittanceRequestDto = RemittanceRequestDto.builder()
			.accountNum(3288494829384L)
			.remittanceAmount(1000000L)
			.build();

		given(httpServletRequest.getSession(false)).willReturn(null);
		// when // then
		assertThrows(UserException.class, () -> accountService.chargeMain(remittanceRequestDto, 1L, httpServletRequest));
	}

	@DisplayName("계좌 충전 시 세션에 등록된 유저와 요청한 유저가 다르다면 충전은 실패한다.")
	@Test
	void chargeMainInvalidSessionId(){
		// given
		Long userId = 1L;
		RemittanceRequestDto remittanceRequestDto = RemittanceRequestDto.builder()
			.accountNum(3288494829384L)
			.remittanceAmount(1000000L)
			.build();
		given(httpServletRequest.getSession(false)).willReturn(httpSession);
		given(httpSession.getAttribute("userId")).willReturn(2L); // 세션의 userId와 다름

		// when // then
		assertThrows(UserException.class, () -> accountService.chargeMain(remittanceRequestDto, userId, httpServletRequest));
	}

	@DisplayName("계좌가 비활성화 상태이면 충전은 실패한다.")
	@Test
	void chargeMainAccountUnavailable(){
		//given
		Long userId = 1L;
		RemittanceRequestDto remittanceRequestDto = RemittanceRequestDto.builder()
			.accountNum(3288494829384L)
			.remittanceAmount(1000000L)
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
		RemittanceRequestDto remittanceRequestDto = RemittanceRequestDto.builder()
			.accountNum(3288494829384L)
			.remittanceAmount(1000000L)
			.build();
		given(httpServletRequest.getSession(false)).willReturn(httpSession);
		given(httpSession.getAttribute("userId")).willReturn(userId);
		given(accountRepository.findByAccountNum(anyLong())).willReturn(mainAccount);

		//when //then
		assertThrows(AccountException.class, () -> accountService.chargeMain(remittanceRequestDto, userId, httpServletRequest));
	}

	// @DisplayName("적금 이체")
	// @Test
	// void savingRemittance() {
	// 	// given
	// 	Long savingId = 2L;
	// 	Account savingAccount = Account.builder()
	// 		.accountNum(987654321L)
	// 		.accountRole(AccountRole.SAVINGS)
	// 		.accountBalance(100000L)
	// 		.accountStatus(AccountStatus.AVAILABLE)
	// 		.dailyChargeLimit(0)
	// 		.user(user)
	// 		.build();
	// 	SavingRequestDto savingRequestDto = new SavingRequestDto(500000);
	// 	given(httpServletRequest.getSession(false)).willReturn(httpSession);
	// 	given(httpSession.getAttribute("userId")).willReturn(savingId);
	// 	given(accountRepository.findById(anyLong())).willReturn(Optional.of(savingAccount));
	// 	given(accountRepository.findMainAccount(anyLong(), any(AccountRole.class))).willReturn(mainAccount);
	// 	given(accountRepository.findUserByAccount(anyLong())).willReturn(user);
	//
	// 	// when
	// 	RemittanceResponseDto response = accountService.savingRemittance(savingId, savingRequestDto, httpServletRequest);
	//
	// 	// then
	// 	assertThat(response.responseMsg()).isEqualTo(RemittanceResponseMsg.SUCCESS.getResponseMsg());
	// 	assertThat(mainAccount.getAccountBalance()).isEqualTo(4500000L); // 500000L 차감
	// 	assertThat(savingAccount.getAccountBalance()).isEqualTo(600000L); // 500000L 추가
	// }
}
