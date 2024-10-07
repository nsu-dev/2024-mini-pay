package org.c4marathon.assignment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.c4marathon.assignment.Exception.InsufficientBalanceException;
import org.c4marathon.assignment.domain.Account;
import org.c4marathon.assignment.domain.AccountType;
import org.c4marathon.assignment.domain.User;
import org.c4marathon.assignment.repository.AccountRepository;
import org.c4marathon.assignment.repository.UserRepository;
import org.c4marathon.assignment.service.AccountService;
import org.c4marathon.assignment.service.QueueService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class AccountControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private AccountService accountService;

	@MockBean
	private QueueService queueService;
	@MockBean
	private AccountRepository accountRepository;
	@Mock
	private UserRepository userRepository;

	private static final Long USER_ID = 1L;
	private static final Long SAVINGS_ACCOUNT_ID = 2L;
	private static final int INITIAL_BALANCE = 1000000; // 초기 잔액
	private static final int MONEY = 500000; // 송금 금액
	private static final int INSUFFICIENT_AMOUNT = 600000; // 부족한 금액

	@Test
	@DisplayName("적금 계좌 추가 성공 테스트")
	public void addSavingsAccount_Success_Test() throws Exception {
		// Given
		doReturn(true).when(accountService).addSavingsAccount(USER_ID, AccountType.SAVINGS, INITIAL_BALANCE);

		// When - API 호출
		mockMvc.perform(post("/users/{userId}/savings", USER_ID)
				.param("type", String.valueOf(AccountType.SAVINGS))
				.param("balance", String.valueOf(INITIAL_BALANCE))
				.contentType(MediaType.APPLICATION_JSON))
			// Then - 적금 계좌 추가 성공 응답 확인
			.andExpect(status().isOk());

		// Verify - 서비스 호출 확인
		verify(accountService, times(1)).addSavingsAccount(USER_ID, AccountType.SAVINGS, INITIAL_BALANCE);
	}

	@Test
	@DisplayName("사용자 메인 계좌에서 적금 계좌로 송금 실패 테스트 - 잔액 부족")
	public void transferToSavings_InsufficientBalance_Test() throws Exception {
		// Given - 잔액 부족으로 예외 발생 설정
		doThrow(new InsufficientBalanceException("잔액이 부족합니다.")).when(accountService)
			.transferToSavings(USER_ID, SAVINGS_ACCOUNT_ID, INSUFFICIENT_AMOUNT);

		// When - API 호출
		mockMvc.perform(post("/users/{userId}/move-to-savings", USER_ID)
				.param("savingsAccountId", String.valueOf(SAVINGS_ACCOUNT_ID))
				.param("money", String.valueOf(INSUFFICIENT_AMOUNT))
				.contentType(MediaType.APPLICATION_JSON))
			// Then - 잔액 부족 예외 응답 확인
			.andExpect(status().isBadRequest())
			.andExpect(content().string("잘못된 요청: 잔액이 부족합니다."));

		// Verify - 송금 시도는 했지만 성공하지 않았음
		verify(accountService, times(1)).transferToSavings(USER_ID, SAVINGS_ACCOUNT_ID, INSUFFICIENT_AMOUNT);
	}

	@Test
	@DisplayName("외부 메인 계좌로 송금 성공 테스트")
	public void transferToExternalMainAccount_Success_Test() throws Exception {
		// Given
		doNothing().when(queueService).addToQueue(any());

		// When - API 호출
		mockMvc.perform(post("/users/{userId}/transfer-to-external/{externalUserId}", USER_ID, 2L)
				.param("money", String.valueOf(MONEY))
				.contentType(MediaType.APPLICATION_JSON))
			// Then - 송금 요청 성공 응답 확인
			.andExpect(status().isOk())
			.andExpect(content().string("송금 요청이 접수되었습니다."));

		// Verify - 큐 서비스 호출 확인
		verify(queueService, times(1)).addToQueue(any());
	}

	@Test
	@DisplayName("100명의 사용자가 동시에 송금하는 테스트")
	public void testConcurrentTransfers() throws InterruptedException {
		int transferMoney = 10000;

		// 스레드를 사용하여 100명의 사용자가 동시에 송금 시도
		ExecutorService executorService = Executors.newFixedThreadPool(100);
		for (int i = 0; i < 100; i++) {
			executorService.submit(() -> {
				try {
					mockMvc.perform(post("/users/{userId}/move-to-savings", USER_ID)
							.param("savingsAccountId", String.valueOf(SAVINGS_ACCOUNT_ID))
							.param("money", String.valueOf(transferMoney))
							.contentType(MediaType.APPLICATION_JSON))
						.andExpect(status().isOk());
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		}

		// 스레드 풀 종료 및 모든 작업이 완료될 때까지 대기
		executorService.shutdown();
		executorService.awaitTermination(1, TimeUnit.MINUTES);
	}

	@Test
	@DisplayName("잔액 부족 시 만원 단위로 충전 후 송금되는 테스트")
	public void testAutomaticChargeAndTransfer() throws Exception {
		int transferMoney = 25000; // 송금 금액은 25,000원 (충전 필요)

		// Given - 메인 계좌의 잔액이 5,000원으로 설정
		// 잔액이 부족한 경우에도 자동 충전 후 송금이 성공한다고 가정
		doReturn(true).when(accountService).transferToSavings(USER_ID, SAVINGS_ACCOUNT_ID, transferMoney);

		// When - API 호출
		mockMvc.perform(post("/users/{userId}/move-to-savings", USER_ID)
				.param("savingsAccountId", String.valueOf(SAVINGS_ACCOUNT_ID))
				.param("money", String.valueOf(transferMoney))
				.contentType(MediaType.APPLICATION_JSON))
			// Then - 송금 성공 확인
			.andExpect(status().isOk());

		// Verify - 송금과 관련된 서비스 메서드가 호출되었는지 확인
		verify(accountService, times(1)).transferToSavings(USER_ID, SAVINGS_ACCOUNT_ID, transferMoney);
	}

	@Test
	@DisplayName("송금 중 예기치 않은 예외 발생 테스트")
	public void transferToSavings_UnexpectedException_Test() throws Exception {
		// Given - 예기치 않은 예외 발생을 설정
		doThrow(new RuntimeException("예기치 않은 오류")).when(accountService)
			.transferToSavings(USER_ID, SAVINGS_ACCOUNT_ID, MONEY);

		// When - API 호출
		mockMvc.perform(post("/users/{userId}/move-to-savings", USER_ID)
				.param("savingsAccountId", String.valueOf(SAVINGS_ACCOUNT_ID))
				.param("money", String.valueOf(MONEY))
				.contentType(MediaType.APPLICATION_JSON))
			// Then - 내부 서버 오류 응답 확인
			.andExpect(status().isInternalServerError())
			.andExpect(content().string("서버 에러: 예기치 않은 오류"));

		// Verify - 송금 시도가 이루어졌지만 실패했음을 확인
		verify(accountService, times(1)).transferToSavings(USER_ID, SAVINGS_ACCOUNT_ID, MONEY);
	}

	@Test
	@DisplayName("하루 송금 한도 초과 테스트")
	public void transferToSavings_ExceedingDailyLimit_Test() throws Exception {
		int exceedingMoney = 4_000_000; // 하루 송금 한도 초과 금액 (예: 3백만원 초과)

		// Given - 하루 송금 한도를 초과할 경우 예외를 던지도록 설정
		doThrow(new IllegalArgumentException("오늘의 충전 한도를 초과했습니다.")).when(accountService)
			.transferToSavings(USER_ID, SAVINGS_ACCOUNT_ID, exceedingMoney);

		// When - API 호출
		mockMvc.perform(post("/users/{userId}/move-to-savings", USER_ID)
				.param("savingsAccountId", String.valueOf(SAVINGS_ACCOUNT_ID))
				.param("money", String.valueOf(exceedingMoney))
				.contentType(MediaType.APPLICATION_JSON))
			// Then - 한도 초과 예외 응답 확인
			.andExpect(status().isBadRequest())
			.andExpect(content().string("잘못된 요청: 오늘의 충전 한도를 초과했습니다."));

		// Verify - 송금 시도는 있었지만 한도 초과로 실패
		verify(accountService, times(1)).transferToSavings(USER_ID, SAVINGS_ACCOUNT_ID, exceedingMoney);
	}

	@Test
	@DisplayName("잘못된 계좌로 송금 시도 시 예외 처리 테스트")
	public void transferToSavings_InvalidAccount_Test() throws Exception {
		// Given - 잘못된 계좌로 송금할 경우 예외를 던지도록 설정
		doThrow(new IllegalArgumentException("계좌를 찾을 수 없습니다.")).when(accountService)
			.transferToSavings(USER_ID, 999L, MONEY); // 999L은 존재하지 않는 계좌 ID

		// When - API 호출
		mockMvc.perform(post("/users/{userId}/move-to-savings", USER_ID)
				.param("savingsAccountId", "999")
				.param("money", String.valueOf(MONEY))
				.contentType(MediaType.APPLICATION_JSON))
			// Then - 잘못된 계좌 예외 확인
			.andExpect(status().isBadRequest())
			.andExpect(content().string("잘못된 요청: 계좌를 찾을 수 없습니다."));

		// Verify - 잘못된 계좌로 송금이 시도되었는지 확인
		verify(accountService, times(1)).transferToSavings(USER_ID, 999L, MONEY);
	}

	@Test
	@DisplayName("정상적인 출금 테스트")
	public void testWithdraw_Success() {
		// Given
		User user = UserFixture.createDefaultUser();
		Account account = new Account(AccountType.MAIN, 1000000, user); // 잔액 1,000,000원
		LocalDate today = LocalDate.now();

		// When
		account.withdraw(500000, today); // 500,000원 출금

		// Then
		assertEquals(500000, account.getBalance()); // 잔액 500,000원 확인
		assertEquals(500000, account.getTodayChargeMoney()); // 당일 출금 금액 500,000원 확인
	}

	@Test
	@DisplayName("잔액 초과 출금 테스트")
	public void testWithdraw_InsufficientBalance() {
		// Given
		User user = UserFixture.createDefaultUser();
		Account account = new Account(AccountType.MAIN, 300000, user); // 잔액 300,000원
		LocalDate today = LocalDate.now();

		// When & Then
		assertThrows(IllegalArgumentException.class, () -> {
			account.withdraw(500000, today); // 500,000원 출금 시도 (잔액 초과)
		});
	}

	@Test
	@DisplayName("하루 충전 한도 초과 테스트")
	public void testWithdraw_ExceedDailyLimit() {
		// Given
		User user = UserFixture.createDefaultUser();
		Account account = new Account(AccountType.MAIN, 5000000, user); // 잔액 5,000,000원
		LocalDate today = LocalDate.now();

		// 하루 충전 한도 내에서 출금 (한도 3,000,000원 중 2,900,000원 출금)
		account.withdraw(2900000, today); // 이미 2,900,000원 출금

		// When & Then
		assertThrows(IllegalArgumentException.class, () -> {
			// 추가로 200,000원을 출금 시도 (출금 한도 초과)
			account.withdraw(200000, today);
		});
	}

	@Test
	@DisplayName("하루 충전 한도 경계 테스트")
	public void transferDailyLimitBoundary_Test() {
		// Given
		User user = UserFixture.createDefaultUser();
		Account mainAccount = new Account(AccountType.MAIN, 3_000_000, user); // 초기 잔액을 3백만원으로 설정
		Account savingsAccount = new Account(AccountType.SAVINGS, 0, user); // 적금 계좌

		user.setMainAccount(mainAccount);
		user.addSavingAccount(savingsAccount);

		when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user)); // 사용자 정보 Mock
		when(accountRepository.save(any(Account.class))).thenAnswer(
			invocation -> invocation.getArgument(0)); // 계좌 저장 Mock

		// When
		accountService.transferToSavings(user.getUserId(), savingsAccount.getAccountId(), 2_999_999);

		// 중간 상태 출력
		System.out.println("메인 계좌 잔액: " + mainAccount.getBalance());
		System.out.println("적금 계좌 잔액: " + savingsAccount.getBalance());

		// Then - 적금 계좌에 금액이 제대로 반영되었는지 확인
		assertEquals(2_999_999, savingsAccount.getBalance());
		assertEquals(1, mainAccount.getBalance());  // 메인 계좌에 남은 금액 확인

		// 중간 상태 출력
		System.out.println("메인 계좌 잔액: " + mainAccount.getBalance());
		System.out.println("적금 계좌 잔액: " + savingsAccount.getBalance());

		// Verify - 메인 계좌와 적금 계좌가 저장되었는지 확인
		verify(accountRepository, times(1)).save(mainAccount);
		verify(accountRepository, times(1)).save(savingsAccount);
	}

	@Test
	@DisplayName("잘못된 매개변수로 송금 요청 시도")
	public void transferMissingParameters_Test() throws Exception {
		// When - 필수 매개변수인 savingsAccountId와 money가 빠진 경우
		mockMvc.perform(post("/users/{userId}/move-to-savings", USER_ID)
				.contentType(MediaType.APPLICATION_JSON))
			// Then - 필수 매개변수가 없기 때문에 400 Bad Request가 나와야 함
			.andExpect(status().isBadRequest());
	}
}
