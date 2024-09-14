package org.c4marathon.assignment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.c4marathon.assignment.domain.Account;
import org.c4marathon.assignment.domain.User;
import org.c4marathon.assignment.repository.AccountRepository;
import org.c4marathon.assignment.repository.UserRepository;
import org.c4marathon.assignment.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class UserServiceUnitTest2 {

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
	@DisplayName("적금 계좌 추가 테스트")
	public void addSavingsAccountTest() {
		// given
		Long userId = 1L;
		String accountType = "Savings Account";
		int initialBalance = 100000;

		// User 객체를 Mock으로 설정
		User mockUser = User.builder()
			.userId(userId)
			.name("이수경")
			.password("lsk123")
			.registrationNum("123456-7890123")
			.build();

		// 저장된 User를 반환하도록 설정
		Mockito.when(userRepository.findById(userId))
			.thenReturn(java.util.Optional.of(mockUser));

		// ArgumentCaptor를 사용해 저장되는 Account 객체를 캡처
		ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);

		// when
		userService.addSavingsAccount(userId, accountType, initialBalance);

		// then
		// 적금 계좌가 AccountRepository에 저장되는지 확인
		verify(accountRepository, times(1)).save(accountCaptor.capture());

		Account savedAccount = accountCaptor.getValue();
		assertNotNull(savedAccount);
		assertEquals(accountType, savedAccount.getAccountType());
		assertEquals(initialBalance, savedAccount.getBalance());
		assertEquals(mockUser, savedAccount.getUser()); // User가 null이 아닌지 확인

		// 계좌가 올바르게 추가되었는지 확인 (User 객체에 계좌가 추가되었는지 체크)
		assertEquals(1, mockUser.getSavingAccounts().size());
		Account addedAccount = mockUser.getSavingAccounts().get(0);
		assertEquals(accountType, addedAccount.getAccountType());
		assertEquals(initialBalance, addedAccount.getBalance());

		// 성공 메시지 출력
		System.out.println("적금 계좌 추가 테스트 성공!");
	}
}
