package org.c4marathon.assignment.account.service;

import static org.assertj.core.api.Assertions.*;
import static org.c4marathon.assignment.account.domain.AccountType.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.c4marathon.assignment.account.domain.Account;
import org.c4marathon.assignment.account.dto.request.ChargeRequestDto;
import org.c4marathon.assignment.account.dto.request.SendRequestDto;
import org.c4marathon.assignment.account.dto.response.ChargeResponseDto;
import org.c4marathon.assignment.account.dto.response.SavingAccountResponseDto;
import org.c4marathon.assignment.account.dto.response.SendResponseDto;
import org.c4marathon.assignment.account.repository.AccountRepository;
import org.c4marathon.assignment.common.exception.runtime.BaseException;
import org.c4marathon.assignment.common.fixture.AccountFixture;
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

	@DisplayName("[메인계좌에서 적금계좌로 송금한다.]")
	@Test
	void sendMoney() {
		// given
		User user = UserFixture.basicUser();
		Account mainAccount = AccountFixture.accountWithTypeAndAmount(user, MAIN_ACCOUNT, 600_000);
		Account savingAccount = AccountFixture.accountWithTypeAndAmount(user, SAVING_ACCOUNT, 0);

		SendRequestDto requestDto = new SendRequestDto(
			mainAccount.getId(),
			MAIN_ACCOUNT.getType(),
			300_000,
			savingAccount.getId(),
			SAVING_ACCOUNT.getType()
		);

		given(accountRepository.findByIdAndType(mainAccount.getId(), MAIN_ACCOUNT)).willReturn(
			Optional.of(mainAccount));
		given(accountRepository.findByIdAndType(savingAccount.getId(), SAVING_ACCOUNT)).willReturn(
			Optional.of(savingAccount));

		// when
		SendResponseDto responseDto = accountService.sendMoney(user, requestDto);

		// then
		assertAll(
			() -> assertThat(responseDto.toAccountMoney()).isEqualTo(300_000),
			() -> assertThat(responseDto.fromAccountMoney()).isEqualTo(300_000)
		);
	}

	@DisplayName("[다른 회원 계좌를 인출하면 예외가 발생한다.]")
	@Test
	void sendMoneyByUser() {
		// given
		User user1 = UserFixture.basicUser();
		User user2 = UserFixture.basicUser();
		Account mainAccount = AccountFixture.accountWithTypeAndAmount(user1, MAIN_ACCOUNT, 600_000);
		Account savingAccount = AccountFixture.accountWithTypeAndAmount(user1, SAVING_ACCOUNT, 0);

		SendRequestDto requestDto = new SendRequestDto(
			mainAccount.getId(),
			MAIN_ACCOUNT.getType(),
			300_000,
			savingAccount.getId(),
			SAVING_ACCOUNT.getType()
		);

		given(accountRepository.findByIdAndType(mainAccount.getId(), MAIN_ACCOUNT)).willReturn(
			Optional.of(mainAccount));
		given(accountRepository.findByIdAndType(savingAccount.getId(), SAVING_ACCOUNT)).willReturn(
			Optional.of(savingAccount));

		// when  // then
		assertThrows(BaseException.class, () -> accountService.sendMoney(user2, requestDto));
	}

	@DisplayName("[메인계좌 금액보다 큰 금액을 인출하면 예외가 발생한다.]")
	@Test
	void SendToMoneyWithIsNotEnoughMoney() {
		// given
		User user1 = UserFixture.basicUser();
		Account mainAccount = AccountFixture.accountWithTypeAndAmount(user1, MAIN_ACCOUNT, 200_000);
		Account savingAccount = AccountFixture.accountWithTypeAndAmount(user1, SAVING_ACCOUNT, 0);

		SendRequestDto requestDto = new SendRequestDto(
			mainAccount.getId(),
			MAIN_ACCOUNT.getType(),
			300_000,
			savingAccount.getId(),
			SAVING_ACCOUNT.getType()
		);

		given(accountRepository.findByIdAndType(mainAccount.getId(), MAIN_ACCOUNT)).willReturn(
			Optional.of(mainAccount));
		given(accountRepository.findByIdAndType(savingAccount.getId(), SAVING_ACCOUNT)).willReturn(
			Optional.of(savingAccount));

		// when  // then
		assertThrows(BaseException.class, () -> accountService.sendMoney(user1, requestDto));
	}

	@DisplayName("[메인계좌에 금액을 충전한다.]")
	@Test
	void chargeMainAccount() {
		// given
		User user = UserFixture.basicUser();
		Account account = AccountFixture.accountWithTypeAndAmount(user, MAIN_ACCOUNT, 300_000);

		ChargeRequestDto requestDto = new ChargeRequestDto(account.getId(), 300_000);

		given(accountRepository.findById(any())).willReturn(Optional.of(account));

		// when
		ChargeResponseDto responseDto = accountService.chargeMainAccount(user, requestDto);

		// then
		assertAll(
			() -> assertThat(responseDto.accountId()).isEqualTo(account.getId()),
			() -> assertThat(responseDto.amount()).isEqualTo(600_000),
			() -> assertThat(responseDto.limitAmount()).isEqualTo(3_000_000 - requestDto.chargeAmount())
		);
	}

	@DisplayName("[메인계좌에 금액을 충전 시 충전금액을 초과하면 예외가 발생한다.]")
	@Test
	void chargeMainAccountWithNotEnoughLimitAmount() {
		// given
		User user = UserFixture.basicUser();
		Account account = AccountFixture.accountWithTypeAndAmount(user, MAIN_ACCOUNT, 300_000);

		ChargeRequestDto requestDto = new ChargeRequestDto(account.getId(), 3_000_000);

		given(accountRepository.findById(any())).willReturn(Optional.of(account));

		// when		// then
		assertThrows(BaseException.class, () -> accountService.chargeMainAccount(user, requestDto));
	}
}
