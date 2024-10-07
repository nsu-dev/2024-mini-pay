package org.c4marathon.assignment.settlement.service;

import static org.assertj.core.api.Assertions.*;
import static org.c4marathon.assignment.domain.settlement.entity.settlement.SettlementRole.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.c4marathon.assignment.domain.account.dto.request.RemittanceRequestDto;
import org.c4marathon.assignment.domain.account.dto.response.RemittanceResponseDto;
import org.c4marathon.assignment.domain.account.entity.account.Account;
import org.c4marathon.assignment.domain.account.entity.account.AccountRole;
import org.c4marathon.assignment.domain.account.entity.account.AccountStatus;
import org.c4marathon.assignment.domain.account.entity.responsemsg.RemittanceResponseMsg;
import org.c4marathon.assignment.domain.account.repository.AccountRepository;
import org.c4marathon.assignment.domain.account.service.AccountService;
import org.c4marathon.assignment.domain.settlement.dto.SettlementMapper;
import org.c4marathon.assignment.domain.settlement.dto.request.SettlementRequestDto;
import org.c4marathon.assignment.domain.settlement.dto.response.SettlementHistoryResponseDto;
import org.c4marathon.assignment.domain.settlement.dto.response.SettlementResponseDto;
import org.c4marathon.assignment.domain.settlement.entity.responsemsg.SettlementErrCode;
import org.c4marathon.assignment.domain.settlement.entity.responsemsg.SettlementResponseMsg;
import org.c4marathon.assignment.domain.settlement.entity.settlement.Settlement;
import org.c4marathon.assignment.domain.settlement.entity.settlement.SettlementRole;
import org.c4marathon.assignment.domain.settlement.entity.settlement.SettlementType;
import org.c4marathon.assignment.domain.settlement.entity.settlement.SettlementUser;
import org.c4marathon.assignment.domain.settlement.exception.SettlementException;
import org.c4marathon.assignment.domain.settlement.repository.SettlementRepository;
import org.c4marathon.assignment.domain.settlement.repository.SettlementUserRepository;
import org.c4marathon.assignment.domain.settlement.service.SettlementService;
import org.c4marathon.assignment.domain.user.entity.responsemsg.UserErrCode;
import org.c4marathon.assignment.domain.user.entity.user.User;
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
class SettlementServiceTest {

	@Mock
	private SettlementUserRepository settlementUserRepository;
	@Mock
	private SettlementRepository settlementRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private AccountRepository accountRepository;

	@Mock
	private HttpServletRequest httpServletRequest;

	@Mock
	private HttpSession httpSession;

	@Mock
	private SettlementMapper settlementMapper;

	@InjectMocks
	private SettlementService settlementService;
	@Mock
	private AccountService accountService;

	private User testUser;
	private User testSender;
	private Settlement testSettlement;
	private SettlementUser testSettlementUser;
	private SettlementUser testSettlementSender;
	private Account mainAccount;

	@BeforeEach
	void setUp() {
		testUser = User.builder()
			.userId(1L)
			.userName("조아빈")
			.userBirth("20000604")
			.userPassword("pw123")
			.build();

		testSender = User.builder()
			.userId(2L)
			.userName("조아빈2")
			.userBirth("20000603")
			.userPassword("pw1231")
			.build();

		testSettlement = Settlement.builder()
			.settlementType(SettlementType.EQUALS)
			.remainingAmount(1001L)
			.remainingUsers(2)
			.build();

		mainAccount = Account.builder()
			.accountNum(3288494829384L)
			.accountRole(AccountRole.MAIN)
			.accountBalance(5000000L)
			.accountStatus(AccountStatus.AVAILABLE)
			.dailyChargeLimit(0)
			.user(testUser)
			.build();
		testSettlementUser = new SettlementUser(SettlementRole.RECEIVER, testUser, testSettlement);
		testSettlementSender = new SettlementUser(SettlementRole.RECEIVER, testSender, testSettlement);
	}

	@DisplayName("모든 정산 내역 조회")
	@Test
	void findAllSettlement() {
		// given
		given(httpServletRequest.getSession(false)).willReturn(httpSession);
		given(httpSession.getAttribute("userId")).willReturn(1L);
		given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
		given(settlementUserRepository.findAllByUser(testUser))
			.willReturn(
				Optional.of(Collections.singletonList(testSettlementUser))
			);

		// when
		List<SettlementHistoryResponseDto> result = settlementService.findAllSettlement(httpServletRequest);

		// then
		assertThat(result).isNotNull();
		assertThat(result.size()).isEqualTo(1);
		then(settlementUserRepository).should(times(1)).findAllByUser(testUser);
	}

	@DisplayName("모든 정산 내역 조회 시 정산내역을 찾지 못하면 예외가 발생한다.")
	@Test
	void findAllSettlementErr() {
		// given
		given(httpServletRequest.getSession(false)).willReturn(httpSession);
		given(httpSession.getAttribute("userId")).willReturn(1L);
		given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
		given(settlementUserRepository.findAllByUser(testUser)).willReturn(Optional.empty());

		// when //then
		SettlementException exception = assertThrows(
			SettlementException.class,
			() -> settlementService.findAllSettlement(httpServletRequest)
		);

		assertEquals(
			exception.getSettlementErrCode().getMessage(),
			SettlementErrCode.SETTLEMENT_NOT_FOUND.getMessage()
		);
	}

	@DisplayName("모든 정산 내역 조회 시 세션이 없다면 예외가 발생한다.")
	@Test
	void findAllSettlementNoSessionErr() {
		// given
		given(httpServletRequest.getSession(false)).willReturn(null);

		// when //then
		UserException exception = assertThrows(
			UserException.class,
			() -> settlementService.findAllSettlement(httpServletRequest)
		);

		assertEquals(
			exception.getUserErrCode().getMessage(),
			UserErrCode.USER_SESSION_ERR.getMessage()
		);
	}

	@DisplayName("모든 정산 내역 조회 시 세션아이디가 없다면 예외가 발생한다.")
	@Test
	void findAllSettlementNoSessionIdErr() {
		// given
		given(httpServletRequest.getSession(false)).willReturn(httpSession);
		given(httpSession.getAttribute("userId")).willReturn(null);
		// when //then
		UserException exception = assertThrows(
			UserException.class,
			() -> settlementService.findAllSettlement(httpServletRequest)
		);

		assertEquals(
			exception.getUserErrCode().getMessage(),
			UserErrCode.USER_SESSION_ERR.getMessage()
		);
	}

	@DisplayName("모든 정산 내역 조회 시 유저를 찾지 못하면 예외가 발생한다.")
	@Test
	void findAllSettlementUserFoundErr() {
		// given
		given(httpServletRequest.getSession(false)).willReturn(httpSession);
		given(httpSession.getAttribute("userId")).willReturn(1L);
		given(userRepository.findById(1L)).willReturn(Optional.empty());

		// when //then
		UserException exception = assertThrows(
			UserException.class,
			() -> settlementService.findAllSettlement(httpServletRequest)
		);

		assertEquals(
			exception.getUserErrCode().getMessage(),
			UserErrCode.USER_NOT_FOUND.getMessage()
		);
	}

	@DisplayName("정산 요청 검증")
	@Test
	void requestSettlement() {
		// given
		User user1 = User.builder().build();
		User user2 = User.builder().build();
		User user3 = User.builder().build();
		SettlementRequestDto settlementRequestDto = new SettlementRequestDto(
			10000L,
			SettlementType.EQUALS,
			Arrays.asList("010-8337-6023", "010-8337-6024", "010-8337-6025")
		);
		given(httpServletRequest.getSession(false)).willReturn(httpSession);
		given(httpSession.getAttribute("userId")).willReturn(1L);
		given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
		given(userRepository.findByUserPhone("010-8337-6023")).willReturn(Optional.of(user1));
		given(userRepository.findByUserPhone("010-8337-6024")).willReturn(Optional.of(user2));
		given(userRepository.findByUserPhone("010-8337-6025")).willReturn(Optional.of(user3));

		// when
		SettlementResponseDto result = settlementService.requestSettlement(settlementRequestDto, httpServletRequest);

		// then
		assertEquals(result.responseMsg(), SettlementResponseMsg.REQUEST_COMPLETED.getResponseMsg());
	}

	@DisplayName("정산 요청 시 유저를 찾지 못하면 예외가 발생한다.")
	@Test
	void requestSettlementUserNotFoundErr() {
		// given
		SettlementRequestDto settlementRequestDto = new SettlementRequestDto(
			10000L,
			SettlementType.EQUALS,
			Arrays.asList("010-8337-6023", "010-8337-6024", "010-8337-6025")
		);
		given(httpServletRequest.getSession(false)).willReturn(httpSession);
		given(httpSession.getAttribute("userId")).willReturn(1L);
		given(userRepository.findById(1L)).willReturn(Optional.empty());

		// when //then
		UserException exception = assertThrows(
			UserException.class, () -> settlementService.requestSettlement(settlementRequestDto, httpServletRequest)
		);
		assertEquals(exception.getUserErrCode().getMessage(), UserErrCode.USER_NOT_FOUND.getMessage());
	}

	@DisplayName("정산 요청 시 정산대상자를 찾지 못하면 예외가 발생한다.")
	@Test
	void requestSettlementUserNotFoundPhoneNumErr() {
		// given
		User user1 = User.builder().build();
		User user2 = User.builder().build();
		SettlementRequestDto settlementRequestDto = new SettlementRequestDto(
			10000L,
			SettlementType.EQUALS,
			Arrays.asList("010-8337-6023", "010-8337-6024", "010-8337-6025")
		);
		given(httpServletRequest.getSession(false)).willReturn(httpSession);
		given(httpSession.getAttribute("userId")).willReturn(1L);
		given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
		given(userRepository.findByUserPhone("010-8337-6023")).willReturn(Optional.of(user1));
		given(userRepository.findByUserPhone("010-8337-6024")).willReturn(Optional.of(user2));
		given(userRepository.findByUserPhone("010-8337-6025")).willReturn(Optional.empty());

		// when //then
		UserException exception = assertThrows(
			UserException.class, () -> settlementService.requestSettlement(settlementRequestDto, httpServletRequest)
		);
		assertEquals(exception.getUserErrCode().getMessage(), UserErrCode.USER_NOT_FOUND.getMessage());
	}

	@DisplayName("n/1 정산 분배")
	@Test
	void settlementSplitEquals() {
		// given
		given(httpServletRequest.getSession(false)).willReturn(httpSession);
		given(httpSession.getAttribute("userId")).willReturn(1L);
		given(settlementUserRepository.findReceiver(RECEIVER, testSettlementUser.getSettlement())).willReturn(testUser);
		given(settlementUserRepository.findById(1L)).willReturn(Optional.of(testSettlementSender));
		given(accountRepository.findMainAccount(1L, AccountRole.MAIN)).willReturn(mainAccount);
		given(accountService.remittanceOtherMain(any(RemittanceRequestDto.class), eq(httpServletRequest)))
			.willReturn(new RemittanceResponseDto(RemittanceResponseMsg.SUCCESS.getResponseMsg()));
		// when
		RemittanceResponseDto result = settlementService.settlementSplit(1L, httpServletRequest);

		// then
		assertEquals(result.responseMsg(), RemittanceResponseMsg.SUCCESS.getResponseMsg());

		// 추가적으로 확인: 남은 금액, 정산 내역 업데이트 여부 등
		assertEquals(testSettlement.getRemainingAmount(), 500L);
		assertEquals(testSettlement.getRemainingUsers(), 1);

		// repository 상호작용 검증
		then(settlementUserRepository).should(times(1)).findById(1L);
		then(accountRepository).should(times(1)).findMainAccount(1L, AccountRole.MAIN);
	}

	@DisplayName("랜덤 정산 분배")
	@Test
	void settlementSplitRandom() {
		// given
		testSettlement = Settlement.builder().settlementType(SettlementType.RANDOM).build();
		given(httpServletRequest.getSession(false)).willReturn(httpSession);
		given(httpSession.getAttribute("userId")).willReturn(1L);
		given(settlementUserRepository.findReceiver(RECEIVER, testSettlementUser.getSettlement())).willReturn(testUser);
		given(settlementUserRepository.findById(1L)).willReturn(Optional.of(testSettlementSender));
		given(accountRepository.findMainAccount(1L, AccountRole.MAIN)).willReturn(mainAccount);
		given(accountService.remittanceOtherMain(any(RemittanceRequestDto.class), eq(httpServletRequest)))
			.willReturn(new RemittanceResponseDto(RemittanceResponseMsg.SUCCESS.getResponseMsg()));
		// when
		RemittanceResponseDto result = settlementService.settlementSplit(1L, httpServletRequest);

		// then
		assertEquals(result.responseMsg(), RemittanceResponseMsg.SUCCESS.getResponseMsg());

		// 추가적으로 확인: 남은 금액, 정산 내역 업데이트 여부 등
		assertThat(testSettlement.getRemainingAmount()).isBetween(1L, 1000L);
		assertEquals(testSettlement.getRemainingUsers(), 1);

		// repository 상호작용 검증
		then(settlementUserRepository).should(times(1)).findById(1L);
		then(accountRepository).should(times(1)).findMainAccount(1L, AccountRole.MAIN);
	}
	//
	// @DisplayName("랜덤 정산 분배")
	// @Test
	// void settlementSplitRandom() {
	// 	// given
	// 	given(accountRepository.findMainAccount(1L, AccountRole.MAIN)).willReturn(null); // 모킹된 데이터
	// 	given(settlementUserRepository.findById(1L)).willReturn(Optional.of(testSettlementUser));
	//
	// 	// when
	// 	RemittanceRequestDto result = settlementService.settlementSplitRandom(testSettlement, testUser);
	//
	// 	// then
	// 	assertThat(result).isNotNull();
	// 	assertThat(result.remittanceAmount()).isBetween(1L, 1000L); // 랜덤값 검증
	// }
	//
	// @DisplayName("정산 대상 사용자 조회 실패 시 예외 발생")
	// @Test
	// void settlementUserNotFound() {
	// 	// given
	// 	given(settlementUserRepository.findById(anyLong())).willReturn(Optional.empty());
	//
	// 	// when // then
	// 	assertThatThrownBy(() -> settlementService.settlementSplit(1L, httpServletRequest))
	// 		.isInstanceOf(SettlementException.class)
	// 		.hasMessage("Settlement user not found");
	// }
}
