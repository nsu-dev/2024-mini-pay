package org.c4marathon.assignment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.c4marathon.assignment.domain.Account;
import org.c4marathon.assignment.domain.AccountType;
import org.c4marathon.assignment.domain.User;
import org.c4marathon.assignment.repository.AccountRepository;
import org.c4marathon.assignment.repository.UserRepository;
import org.c4marathon.assignment.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class AccountServiceTest {

	@InjectMocks
	private AccountService accountService;

	@Mock
	private UserRepository userRepository;

	@Mock
	private AccountRepository accountRepository;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	private static final Long USER_ID = 1L;
	private static final Long SAVINGS_ACCOUNT_ID = 2L;

	@Test
	@DisplayName("메인 계좌에서 적금 계좌로 송금 테스트")
	public void transferToSavings_Success() {
		// Given
		User user = UserFixture.createDefaultUser(); // 기본 사용자 생성
		Account mainAccount = new Account(1L, AccountType.MAIN, 1000000, user); // 메인 계좌 잔액 1,000,000원, ID 1
		Account savingsAccount = new Account(2L, AccountType.SAVINGS, 500000, user); // 적금 계좌 잔액 500,000원, ID 2

		user.setMainAccount(mainAccount); // 메인 계좌 설정
		user.addSavingAccount(savingsAccount); // 적금 계좌 추가

		// UserRepository와 AccountRepository에 대한 Mock 설정
		when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));
		when(accountRepository.findById(mainAccount.getAccountId())).thenReturn(Optional.of(mainAccount));
		when(accountRepository.findById(savingsAccount.getAccountId())).thenReturn(Optional.of(savingsAccount));

		// save 호출에 대한 Mock 설정 (반환값이 필요하면 명시적으로 설정)
		when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

		// When
		accountService.transferToSavings(user.getUserId(), savingsAccount.getAccountId(), 200000); // 200,000원 송금

		// Then
		assertEquals(800000, mainAccount.getBalance()); // 메인 계좌 잔액 확인 (1,000,000 - 200,000)
		assertEquals(700000, savingsAccount.getBalance()); // 적금 계좌 잔액 확인 (500,000 + 200,000)

		verify(accountRepository, times(1)).save(mainAccount); // 메인 계좌 저장 호출 확인
		verify(accountRepository, times(1)).save(savingsAccount); // 적금 계좌 저장 호출 확인
	}

	@Test
	@DisplayName("0원 송금 시도 테스트")
	public void transferZeroAmount_Test() {
		// Given
		int zeroAmount = 0;

		// When & Then
		assertThrows(IllegalArgumentException.class, () -> {
			accountService.transferToSavings(USER_ID, SAVINGS_ACCOUNT_ID, zeroAmount);
		});
	}

	//Account Service 함수들 테스트
	@Test
	@DisplayName("외부 계좌에서 돈을 가져오는 테스트")
	public void testTransferFromExternalAccount() {
		// Given
		User user = UserFixture.createDefaultUser();
		User externalUser = UserFixture.createExternalUser(2L);
		Account externalAccount = new Account(AccountType.MAIN, 1_000_000, externalUser);
		externalUser.setMainAccount(externalAccount);

		when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));
		when(userRepository.findById(externalUser.getUserId())).thenReturn(Optional.of(externalUser));

		// When
		boolean result = accountService.transferFromExternalAccount(user.getUserId(), externalUser.getUserId(),
			500_000);

		// Then
		assertTrue(result);

		// 테스트 도중 값 확인
		System.out.println("사용자 메인 계좌 잔액: " + user.getMainAccount().getBalance());
		System.out.println("외부 메인 계좌 잔액: " + externalAccount.getBalance());

		assertEquals(1_500_000, user.getMainAccount().getBalance());  // 사용자 계좌 잔액 확인
		assertEquals(500_000, externalAccount.getBalance());  // 외부 계좌 잔액 확인
	}

	@Test
	@DisplayName("외부 계좌로 송금 테스트")
	public void testTransferToExternalMainAccount() {
		// Given
		User user = UserFixture.createDefaultUser(); // 기본 사용자 생성
		User externalUser = UserFixture.createExternalUser(2L); // 외부 사용자 생성

		Account userMainAccount = new Account(1L, AccountType.MAIN, 700_000, user); // 사용자 메인 계좌 (잔액 700,000원)
		Account externalAccount = new Account(2L, AccountType.MAIN, 500_000, externalUser); // 외부 메인 계좌 (잔액 500,000원)

		user.setMainAccount(userMainAccount); // 사용자 메인 계좌 설정
		externalUser.setMainAccount(externalAccount); // 외부 사용자 메인 계좌 설정

		// UserRepository, AccountRepository에 대한 Mock 설정
		when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));
		when(userRepository.findById(externalUser.getUserId())).thenReturn(Optional.of(externalUser));

		// save 호출에 대한 Mock 설정 (반환값이 필요하면 명시적으로 설정)
		when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

		// When
		boolean result = accountService.transferToExternalMainAccount(user.getUserId(), externalUser.getUserId(),
			300_000); // 300,000원 송금

		// Then
		assertTrue(result);
		assertEquals(400_000, userMainAccount.getBalance()); // 사용자 메인 계좌 잔액 확인 (700,000 - 300,000 = 400,000원)
		assertEquals(800_000, externalAccount.getBalance()); // 외부 계좌 잔액 확인 (500,000 + 300,000 = 800,000원)

		// 저장 호출 확인
		verify(accountRepository, times(1)).save(userMainAccount);  // 사용자 메인 계좌 저장 호출 확인
		verify(accountRepository, times(1)).save(externalAccount);  // 외부 계좌 저장 호출 확인
	}

	@Test
	@DisplayName("자동 충전 처리 테스트")
	public void testHandleAutoCharge() {
		// Given
		Account mainAccount = new Account(AccountType.MAIN, 5_000, UserFixture.createDefaultUser());

		// When
		accountService.handleAutoCharge(mainAccount, 15_000);

		// Then
		assertEquals(15_000, mainAccount.getBalance());
	}

	@Test
	@DisplayName("메인 계좌 확인 테스트")
	public void testCheckMainAccount() {
		// Given
		Account mainAccount = new Account(AccountType.MAIN, 1_000_000, UserFixture.createDefaultUser());
		Account savingsAccount = new Account(AccountType.SAVINGS, 500_000, UserFixture.createDefaultUser());

		// When & Then
		assertThrows(IllegalArgumentException.class, () -> {
			accountService.checkMainAccount(savingsAccount);
		});
		accountService.checkMainAccount(mainAccount);  // Should not throw exception
	}

	@Test
	@DisplayName("적금 계좌 추가 테스트")
	public void testAddSavingsAccount() {
		// Given
		User user = UserFixture.createDefaultUser();
		when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));

		// When
		boolean result = accountService.addSavingsAccount(user.getUserId(), AccountType.SAVINGS, 500_000);

		// Then
		assertTrue(result);
		assertNotNull(user.getSavingAccounts());
		verify(userRepository, times(1)).save(user);
	}

	@Test
	@DisplayName("충전 한도 확인 테스트")
	public void testCheckTransferLimit() {
		// Given
		Account mainAccount = new Account(AccountType.MAIN, 1_000_000, UserFixture.createDefaultUser());

		// When & Then
		assertThrows(IllegalArgumentException.class, () -> {
			accountService.checkTransferLimit(mainAccount, 5_000_000);  // 한도 초과
		});

		accountService.checkTransferLimit(mainAccount, 500_000);  // 한도 내 금액
	}

	@Test
	@DisplayName("적금 계좌 생성 및 저장 테스트")
	public void testCreateAndSaveAccount() {
		// Given
		User user = UserFixture.createDefaultUser();
		when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

		// When
		Account savingsAccount = accountService.createAndSaveAccount(user, AccountType.SAVINGS, 500_000);

		// Then
		assertNotNull(savingsAccount);
		assertEquals(500_000, savingsAccount.getBalance());
		assertEquals(AccountType.SAVINGS, savingsAccount.getType());
	}

}
