package org.c4marathon.assignment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.c4marathon.assignment.Dto.UserRequestDto;
import org.c4marathon.assignment.Dto.UserResponseDto;
import org.c4marathon.assignment.domain.Account;
import org.c4marathon.assignment.domain.AccountType;
import org.c4marathon.assignment.domain.User;
import org.c4marathon.assignment.repository.AccountRepository;
import org.c4marathon.assignment.repository.UserRepository;
import org.c4marathon.assignment.service.AccountService;
import org.c4marathon.assignment.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private AccountRepository accountRepository;

	@InjectMocks
	private UserService userService;

	@InjectMocks
	private AccountService accountService;

	private User user;
	private User externalUser;
	private Account userMainAccount;
	private Account externalMainAccount;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		user = new User(1L, "lsk123", "이수경", "123456-7890123");
		externalUser = new User(2L, "kim123", "김철수", "987654-3210987");

		userMainAccount = new Account(AccountType.MAIN, 1000000, user);
		externalMainAccount = new Account(AccountType.MAIN, 2000000, externalUser);

		user.setMainAccount(userMainAccount);
		externalUser.setMainAccount(externalMainAccount);

		// 정확한 사용자 및 계좌 반환 설정
		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		when(userRepository.findById(2L)).thenReturn(Optional.of(externalUser));

		when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));
		when(userRepository.findById(externalUser.getUserId())).thenReturn(Optional.of(externalUser));
	}

	@Test
	@DisplayName("회원가입 단위 테스트")
	public void registerUserTest() {
		// given
		UserRequestDto userRequestDto = new UserRequestDto(
			null,
			"lsk123",
			"이수경",
			"123456-789123"
		);

		// User 객체를 생성자 방식으로 생성
		User mockUser = new User(1L, "lsk123", "이수경", "123456-7890123");
		Account mockAccount = new Account(AccountType.MAIN, 0, mockUser);
		mockUser.setMainAccount(mockAccount);

		// mock repository behaviors
		when(userRepository.save(any(User.class))).thenReturn(mockUser);

		// when
		UserResponseDto responseDto = userService.registerUser(userRequestDto);

		// then
		assertNotNull(responseDto);
		assertEquals(1L, responseDto.getUserId());
		assertEquals("이수경", responseDto.getName());
		assertEquals("123456-789123", responseDto.getRegistrationNum());
		assertNotNull(mockUser.getMainAccount());
		assertEquals(AccountType.MAIN, mockUser.getMainAccount().getType());
		assertEquals(0, mockUser.getMainAccount().getBalance());

		// 성공 시 문구 출력
		System.out.println("회원가입 단위 테스트 성공!");
	}

	@Test
	@DisplayName("적금 계좌 추가 테스트")
	public void addSavingsAccountTest() {
		// given
		Long userId = 1L;
		AccountType type = AccountType.SAVINGS;
		int initialBalance = 100000;

		// User 객체를 Mock으로 설정
		User mockUser = new User(1L, "lsk123", "이수경", "123456-7890123");

		when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(mockUser));

		ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);

		// when
		accountService.addSavingsAccount(userId, type, initialBalance);

		// then
		verify(accountRepository, times(1)).save(accountCaptor.capture());

		Account savedAccount = accountCaptor.getValue();
		assertNotNull(savedAccount);
		assertEquals(type, savedAccount.getType());
		assertEquals(initialBalance, savedAccount.getBalance());
		assertEquals(mockUser, savedAccount.getUser());

		assertEquals(1, mockUser.getSavingAccounts().size());
		Account addedAccount = mockUser.getSavingAccounts().get(0);
		assertEquals(type, addedAccount.getType());
		assertEquals(initialBalance, addedAccount.getBalance());

		System.out.println("적금 계좌 추가 테스트 성공!");
	}

	@Test
	@DisplayName("메인 계좌에서 잔액 부족으로 송금 실패 테스트")
	public void transferToSavingsFailTest() {
		// given
		Long userId = 1L;
		Long savingsAccountId = 2L;
		int transferMoney = 150000; // 메인 계좌 잔액보다 많은 금액

		// 사용자와 계좌 객체 Mock 설정
		User mockUser = new User(
			1L, "lsk123", "이수경", "123456-7890123"
		);

		Account mainAccount = new Account(AccountType.MAIN, 100000, mockUser); // 메인 계좌에 100,000원
		Account savingsAccount = new Account(AccountType.SAVINGS, 0, mockUser); // 적금 계좌는 0원

		mockUser.setMainAccount(mainAccount);

		// 저장된 사용자와 계좌 반환 설정
		when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

		// lenient()로 불필요한 stubbing 경고 무시
		lenient().when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
		lenient().when(accountRepository.findById(savingsAccountId)).thenReturn(Optional.of(savingsAccount));

		// when & then (잔액 부족으로 예외 발생 확인)
		assertThrows(IllegalArgumentException.class, () -> {
			accountService.transferToSavings(userId, savingsAccountId, transferMoney);
		});

		// 성공 메시지 출력
		System.out.println("메인 계좌에서 잔액 부족으로 송금 실패 테스트 성공!");
	}

	@Test
	@DisplayName("외부 유저의 메인 계좌에서 사용자 메인 계좌로 돈 이동 성공 테스트")
	public void transferSuccessTest() {
		// given
		Long userId = 1L;
		Long externalUserId = 2L;
		int transferMoney = 500000;

		// when
		boolean success = accountService.transferFromExternalAccount(userId, externalUserId, transferMoney);

		// then
		assertTrue(success);
		assertEquals(1000000, userMainAccount.getBalance()); // 사용자의 메인 계좌 잔액
		assertEquals(2000000, externalMainAccount.getBalance()); // 외부 유저의 메인 계좌 잔액

		// 성공 시 문구 출력
		System.out.println("돈 이동 성공 테스트 성공!");
	}

	@Test
	@DisplayName("외부 유저의 잔액 부족으로 인한 돈 이동 실패 테스트")
	public void transferInsufficientFundsTest() {
		// given
		Long userId = 1L;
		Long externalUserId = 2L;
		int transferMoney = 500000;

		// 외부 유저의 잔액 부족 설정
		externalMainAccount = new Account(AccountType.MAIN, 300000, externalUser);
		externalUser.setMainAccount(externalMainAccount);

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(userRepository.findById(externalUserId)).thenReturn(Optional.of(externalUser));

		// when & then (잔액 부족으로 예외 발생 확인)
		assertThrows(IllegalArgumentException.class, () -> {
			accountService.transferFromExternalAccount(userId, externalUserId, transferMoney);
		});

		// 성공 메시지 출력
		System.out.println("잔액 부족으로 인한 돈 이동 실패 테스트 성공!");
	}

	@Test
	@DisplayName("메인 계좌에서 외부 메인 계좌로 송금 성공 테스트")
	public void transferToExternalSuccessTest() {
		Long userId = 1L;
		Long externalUserId = 2L;
		int transferMoney = 500000;

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(userRepository.findById(externalUserId)).thenReturn(Optional.of(externalUser));

		// 송금 성공 테스트
		boolean success = accountService.transferToExternalMainAccount(userId, externalUserId, transferMoney);

		assertTrue(success);
		assertEquals(500000, userMainAccount.getBalance());
		assertEquals(2500000, externalMainAccount.getBalance());

		System.out.println("메인 계좌에서 외부 메인 계좌로 송금 성공 테스트 성공!");
	}

	@Test
	@DisplayName("100명의 사용자가 동시에 송금하는 테스트")
	public void testConcurrentTransfers() throws InterruptedException {
		int transferMoney = 10000;
		ExecutorService executorService = Executors.newFixedThreadPool(100);

		// 100명의 사용자가 동시에 송금 시도
		for (int i = 0; i < 100; i++) {
			executorService.submit(() -> {
				boolean success = accountService.transferToSavings(user.getUserId(), userMainAccount.getAccountId(),
					transferMoney);
				assertTrue(success);
			});
		}

		// 스레드 풀 종료 및 모든 작업이 완료될 때까지 대기
		executorService.shutdown();
		executorService.awaitTermination(1, TimeUnit.MINUTES);

		// 최종 잔액이 0이 되는지 확인 (총 1,000,000원에서 100명의 송금이 10000원씩 이루어짐)
		assertEquals(0, userMainAccount.getBalance(), "송금 후 잔액이 0이어야 합니다.");
	}

	@Test
	@DisplayName("잔액 부족 시 만원 단위로 충전 후 송금되는 테스트")
	public void testAutomaticChargeAndTransfer() {
		int transferMoney = 25000; // 송금 금액은 25,000원 (충전 필요)

		// 충전 전 계좌 잔액이 부족한지 확인
		assertEquals(5000, userMainAccount.getBalance());

		boolean success = accountService.transferToSavings(user.getUserId(), userMainAccount.getAccountId(),
			transferMoney);

		// 송금 성공 여부 확인
		assertTrue(success);

		// 충전 후 최종 잔액 확인 (25,000원 송금 후 잔액 0원)
		assertEquals(0, userMainAccount.getBalance());

		// 총 충전된 금액 확인 (20,000원이 충전되어야 함)
		assertEquals(20000, userMainAccount.getTodayChargeMoney());

		System.out.println("잔액 부족 시 자동 충전 후 송금 성공!");
	}
}