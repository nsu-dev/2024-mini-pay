package org.c4marathon.assignment.domain.account.service;

import org.c4marathon.assignment.domain.account.repository.AccountRepository;
import org.c4marathon.assignment.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class AccountServiceTest {

	@Mock
	private AccountRepository accountRepository;

	@InjectMocks
	private AccountService accountService;

	private User user;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		user = new User();
	}

	// @DisplayName("메인계좌 생성이 잘 되는지 확인한다.")
	// @Test
	// void createMain() {
	// 	// given
	// 	String randomAccountNum = "3000000000001";
	// 	when(accountRepository.existsByAccountNum(randomAccountNum)).thenReturn(false);
	//
	// 	// when
	// 	accountService.createMain(user);
	//
	// 	// then
	// 	verify(accountRepository, times(1)).save(any(Account.class));
	// }

	// @Test
	// @DisplayName("메인계좌 생성 시 중복된 계좌가 있다면 메서드가 재호출 되는지 확인한다.")
	// void DuplicateAccountNumber() {
	// 	// given
	// 	String randomAccountNum = "3000000000001";
	// 	when(accountRepository.existsByAccountNum(randomAccountNum)).thenReturn(true);
	//
	// 	// when
	// 	accountService.createMain(user);
	//
	// 	// then
	// 	verify(accountRepository, times(1)).save(any(Account.class));
	// }
}
