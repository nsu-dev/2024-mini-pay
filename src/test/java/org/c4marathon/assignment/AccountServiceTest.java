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
}
