package org.c4marathon.assignment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.c4marathon.assignment.domain.Account;
import org.c4marathon.assignment.domain.User;
import org.c4marathon.assignment.repository.AccountRepository;
import org.c4marathon.assignment.repository.UserRepository;
import org.c4marathon.assignment.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class UserServiceUnitTest3 {

	@Mock
	private UserRepository userRepository;

	@Mock
	private AccountRepository accountRepository;

	@InjectMocks
	private UserService userService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this); // Mockito 초기화
	}

	@Test
	@DisplayName("메인 계좌에서 적금 계좌로 송금 테스트")
	public void transferToSavingsTest() {
		// given
		Long userId = 1L;
		Long savingsAccountId = 2L;
		int transferAmount = 50000;

		// 사용자와 계좌 객체 Mock 설정
		User mockUser = User.builder()
			.userId(userId)
			.name("이수경")
			.password("lsk123")
			.registrationNum("123456-7890123")
			.build();

		Account mainAccount = new Account("Main Account", 100000, mockUser); // 메인 계좌에 100,000원
		Account savingsAccount = new Account("Savings Account", 0, mockUser); // 적금 계좌는 0원

		mockUser.setMainAccount(mainAccount);

		// 저장된 사용자와 계좌 반환 설정
		when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
		when(accountRepository.findById(savingsAccountId)).thenReturn(Optional.of(savingsAccount));

		// when
		boolean result = userService.transferToSavings(userId, savingsAccountId, transferAmount);

		// then
		assertTrue(result);

		// 출금 및 입금 확인
		assertEquals(50000, mainAccount.getBalance()); // 메인 계좌에서 50,000원이 출금되었는지 확인
		assertEquals(50000, savingsAccount.getBalance()); // 적금 계좌에 50,000원이 입금되었는지 확인

		// 계좌가 저장되는지 확인
		verify(accountRepository, times(1)).save(mainAccount);
		verify(accountRepository, times(1)).save(savingsAccount);

		// 성공 메시지 출력
		System.out.println("메인 계좌에서 적금 계좌로 송금 테스트 성공!");
	}

	@Test
	@DisplayName("메인 계좌에서 잔액 부족으로 송금 실패 테스트")
	public void transferToSavingsFailTest() {
		// given
		Long userId = 1L;
		Long savingsAccountId = 2L;
		int transferAmount = 150000; // 메인 계좌 잔액보다 많은 금액

		// 사용자와 계좌 객체 Mock 설정
		User mockUser = User.builder()
			.userId(userId)
			.name("이수경")
			.password("lsk123")
			.registrationNum("123456-7890123")
			.build();

		Account mainAccount = new Account("Main Account", 100000, mockUser); // 메인 계좌에 100,000원
		Account savingsAccount = new Account("Savings Account", 0, mockUser); // 적금 계좌는 0원

		mockUser.setMainAccount(mainAccount);

		// 저장된 사용자와 계좌 반환 설정
		when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
		when(accountRepository.findById(savingsAccountId)).thenReturn(Optional.of(savingsAccount));

		// when & then (잔액 부족으로 예외 발생 확인)
		assertThrows(IllegalArgumentException.class, () -> {
			userService.transferToSavings(userId, savingsAccountId, transferAmount);
		});

		// 성공 메시지 출력
		System.out.println("메인 계좌에서 잔액 부족으로 송금 실패 테스트 성공!");
	}
}