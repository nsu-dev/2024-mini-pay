package org.c4marathon.assignment.account.service;

import static org.assertj.core.api.Assertions.*;
import static org.c4marathon.assignment.account.domain.AccountType.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.c4marathon.assignment.account.dto.response.SavingAccountResponseDto;
import org.c4marathon.assignment.account.repository.AccountRepository;
import org.c4marathon.assignment.common.fixture.UserFixture;
import org.c4marathon.assignment.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

	@Mock
	private AccountRepository accountRepository;

	@InjectMocks
	private AccountService accountService;

	@DisplayName("[적금계좌를 생성한다]")
	@Test
	void generateSavingAccount() {
		// given
		User user = UserFixture.basicUser();

		// when
		SavingAccountResponseDto response = accountService.generateSavingAccount(user);

		// then
		verify(accountRepository).save(argThat(account ->
			account.getUser().equals(user) &&
				account.getType().equals(SAVING_ACCOUNT) &&
				account.getAmount() == 0 &&
				account.getLimitAmount() == 3_000_000
		));

		assertAll(
			() -> assertThat(response.userName()).isEqualTo(user.getName()),
			() -> assertThat(response.userEmail()).isEqualTo(user.getEmail())
		);
	}
}
