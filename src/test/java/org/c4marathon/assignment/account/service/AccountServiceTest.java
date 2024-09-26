package org.c4marathon.assignment.account.service;

import static org.assertj.core.api.Assertions.*;
import static org.c4marathon.assignment.account.domain.AccountType.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import org.c4marathon.assignment.account.domain.Account;
import org.c4marathon.assignment.account.dto.request.ChargeRequestDto;
import org.c4marathon.assignment.account.dto.request.SendToOthersRequestDto;
import org.c4marathon.assignment.account.dto.request.SendToSavingAccountRequestDto;
import org.c4marathon.assignment.account.dto.response.AccountResponseDto;
import org.c4marathon.assignment.account.dto.response.ChargeResponseDto;
import org.c4marathon.assignment.account.dto.response.SavingAccountResponseDto;
import org.c4marathon.assignment.account.dto.response.SendToSavingAccountResponseDto;
import org.c4marathon.assignment.account.repository.AccountRepository;
import org.c4marathon.assignment.common.exception.runtime.BaseException;
import org.c4marathon.assignment.common.fixture.AccountFixture;
import org.c4marathon.assignment.common.fixture.UserFixture;
import org.c4marathon.assignment.event.account.WithdrawalFailEvent;
import org.c4marathon.assignment.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

	@Mock
	private AccountRepository accountRepository;

	@Mock
	private ApplicationEventPublisher eventPublisher;

	@InjectMocks
	private AccountService accountService;

	@DisplayName("[적금계좌를 생성한다]")
	@Test
	void generateSavingAccount() {
		// given
		User owner = UserFixture.basicUser();

		// when
		SavingAccountResponseDto response = accountService.generateSavingAccount(owner);

		// then
		verify(accountRepository).save(argThat(account ->
			account.getUser().equals(owner) &&
				account.getType().equals(SAVING_ACCOUNT) &&
				account.getAmount() == 0 &&
				account.getLimitAmount() == 3_000_000
		));

		assertAll(
			() -> assertThat(response.userName()).isEqualTo(owner.getName()),
			() -> assertThat(response.userEmail()).isEqualTo(owner.getEmail())
		);
	}

	@DisplayName("[메인 계좌에서 적금 계좌로 송금한다.]")
	@Test
	void sendMoney() {
		// given
		User owner = UserFixture.basicUser();
		Account mainAccount = AccountFixture.accountWithTypeAndAmount(owner, MAIN_ACCOUNT, 600_000);
		Account savingAccount = AccountFixture.accountWithTypeAndAmount(owner, SAVING_ACCOUNT, 0);

		ReflectionTestUtils.setField(mainAccount, "id", 1L);
		ReflectionTestUtils.setField(savingAccount, "id", 2L);

		SendToSavingAccountRequestDto requestDto = new SendToSavingAccountRequestDto(
			mainAccount.getId(),
			300_000,
			savingAccount.getId()
		);

		given(accountRepository.findById(mainAccount.getId())).willReturn(
			Optional.of(mainAccount));
		given(accountRepository.findById(savingAccount.getId())).willReturn(
			Optional.of(savingAccount));

		// when
		SendToSavingAccountResponseDto responseDto = accountService.sendMoney(owner, requestDto);

		// then
		assertAll(
			() -> assertThat(responseDto.toAccountMoney()).isEqualTo(300_000),
			() -> assertThat(responseDto.fromAccountMoney()).isEqualTo(300_000)
		);
	}

	@DisplayName("[다른 회원 계좌를 인출 하면 예외가 발생한다.]")
	@Test
	void sendMoneyByUser() {
		// given
		User owner = UserFixture.basicUser();
		User others = UserFixture.basicUser();
		ReflectionTestUtils.setField(others, "id", 20L);

		Account mainAccount = AccountFixture.accountWithTypeAndAmount(owner, MAIN_ACCOUNT, 600_000);
		Account savingAccount = AccountFixture.accountWithTypeAndAmount(owner, SAVING_ACCOUNT, 0);

		SendToSavingAccountRequestDto requestDto = new SendToSavingAccountRequestDto(
			mainAccount.getId(),
			300_000,
			savingAccount.getId()
		);

		given(accountRepository.findById(mainAccount.getId())).willReturn(
			Optional.of(mainAccount));
		given(accountRepository.findById(savingAccount.getId())).willReturn(
			Optional.of(savingAccount));

		// when
		BaseException baseException = assertThrows(
			BaseException.class, () -> accountService.sendMoney(others, requestDto)
		);

		// then
		assertThat(baseException.getMessage()).isEqualTo("계좌 인출 권한이 없습니다.");
	}

	@DisplayName("[메인 계좌 금액보다 큰 금액을 인출 할 때 예외가 발생한다.]")
	@Test
	void SendToMoneyWithIsNotEnoughMoney() {
		// given
		User owner = UserFixture.basicUser();
		Account mainAccount = AccountFixture.accountWithTypeAndAmount(owner, MAIN_ACCOUNT, 200_000);
		Account savingAccount = AccountFixture.accountWithTypeAndAmount(owner, SAVING_ACCOUNT, 0);

		SendToSavingAccountRequestDto requestDto = new SendToSavingAccountRequestDto(
			mainAccount.getId(),
			300_000,
			savingAccount.getId()
		);

		given(accountRepository.findById(mainAccount.getId())).willReturn(
			Optional.of(mainAccount));
		given(accountRepository.findById(savingAccount.getId())).willReturn(
			Optional.of(savingAccount));

		// when
		BaseException baseException = assertThrows(BaseException.class,
			() -> accountService.sendMoney(owner, requestDto));

		// then
		assertThat(baseException.getMessage()).isEqualTo("계좌 금액이 충분하지 않습니다.");
	}

	@DisplayName("[메인 계좌에 금액을 충전한다.]")
	@Test
	void chargeMainAccount() {
		// given
		User owner = UserFixture.basicUser();
		Account account = AccountFixture.accountWithTypeAndAmount(owner, MAIN_ACCOUNT, 300_000);

		ChargeRequestDto requestDto = new ChargeRequestDto(account.getId(), 300_000);

		given(accountRepository.findById(any())).willReturn(Optional.of(account));

		// when
		ChargeResponseDto responseDto = accountService.chargeMainAccount(owner, requestDto);

		// then
		assertAll(
			() -> assertThat(responseDto.accountId()).isEqualTo(account.getId()),
			() -> assertThat(responseDto.chargedAmount()).isEqualTo(600_000),
			() -> assertThat(responseDto.limitAmount()).isEqualTo(3_000_000 - requestDto.chargeAmount())
		);
	}

	@DisplayName("[메인 계좌에 금액 충전 시 충전 금액을 초과하면 예외가 발생한다.]")
	@Test
	void chargeMainAccountWithNotEnoughLimitAmount() {
		// given
		User owner = UserFixture.basicUser();
		Account account = AccountFixture.accountWithTypeAndAmount(owner, MAIN_ACCOUNT, 300_000);

		ChargeRequestDto requestDto = new ChargeRequestDto(account.getId(), 4_000_000);

		given(accountRepository.findById(any())).willReturn(Optional.of(account));

		// when
		BaseException baseException = assertThrows(
			BaseException.class, () -> accountService.chargeMainAccount(owner, requestDto)
		);

		// then
		assertThat(baseException.getMessage()).isEqualTo("충전 한도를 초과했습니다.");
	}

	@DisplayName("[타인의 메인 계좌에 금액 충전 시 예외가 발생한다.]")
	@Test
	void chargeMainAccountWithNotOwner() {
		// given
		User owner = UserFixture.basicUser();
		User others = UserFixture.basicUser();
		ReflectionTestUtils.setField(owner, "id", 1L);
		ReflectionTestUtils.setField(others, "id", 2L);
		Account account = AccountFixture.accountWithTypeAndAmount(owner, MAIN_ACCOUNT, 300_000);

		ChargeRequestDto requestDto = new ChargeRequestDto(account.getId(), 4_000_000);

		given(accountRepository.findById(any())).willReturn(Optional.of(account));

		// when
		BaseException baseException = assertThrows(
			BaseException.class, () -> accountService.chargeMainAccount(others, requestDto)
		);

		// then
		assertThat(baseException.getMessage()).isEqualTo("계좌 인출 권한이 없습니다.");
	}

	@DisplayName("[적금 계좌에 금액 충전 시 예외가 발생한다.]")
	@Test
	void chargeMainAccountWithSavingAccount() {
		// given
		User owner = UserFixture.basicUser();
		Account account = AccountFixture.accountWithTypeAndAmount(owner, SAVING_ACCOUNT, 300_000);

		ChargeRequestDto requestDto = new ChargeRequestDto(account.getId(), 4_000_000);

		given(accountRepository.findById(any())).willReturn(Optional.of(account));

		// when
		BaseException baseException = assertThrows(
			BaseException.class, () -> accountService.chargeMainAccount(owner, requestDto)
		);

		// then
		assertThat(baseException.getMessage()).isEqualTo("계좌 금액 충전에 접근할 수 없는 계좌입니다.");
	}

	@DisplayName("[회원의 모든 계좌를 읽어온다.]")
	@Test
	void getAccounts() {
		// given
		User owner = UserFixture.basicUser();
		Account mainAccount = AccountFixture.accountWithTypeAndAmount(owner, MAIN_ACCOUNT, 300_000);
		Account savingAccount1 = AccountFixture.accountWithTypeAndAmount(owner, SAVING_ACCOUNT, 100_000);
		Account savingAccount2 = AccountFixture.accountWithTypeAndAmount(owner, SAVING_ACCOUNT, 200_000);

		List<Account> accounts = List.of(mainAccount, savingAccount1, savingAccount2);

		given(accountRepository.findAllByUser(any(User.class))).willReturn(accounts);

		// when
		List<AccountResponseDto> accountResponseDto = accountService.getAccounts(owner);

		// then
		assertAll(
			() -> assertThat(accountResponseDto).extracting(AccountResponseDto::type)
				.containsExactly(
					MAIN_ACCOUNT.getType(),
					SAVING_ACCOUNT.getType(),
					SAVING_ACCOUNT.getType()
				),
			() -> assertThat(accountResponseDto).extracting(AccountResponseDto::amount)
				.containsExactly(
					300_000,
					100_000,
					200_000
				)
		);
	}

	@DisplayName("[메인 계좌에서 요청 금액만큼 출금한다.]")
	@Test
	void withdrawal() {
		// given
		User owner = UserFixture.basicUser();
		Account mainAccount = AccountFixture.accountWithTypeAndAmount(owner, MAIN_ACCOUNT, 300_000);
		SendToOthersRequestDto requestDto = new SendToOthersRequestDto(
			mainAccount.getId(),
			100_000
		);

		given(accountRepository.findById(mainAccount.getId()))
			.willReturn(Optional.of(mainAccount));

		// when
		accountService.withdrawal(owner, requestDto);

		// then
		assertThat(mainAccount.getAmount()).isEqualTo(200_000);
	}

	@DisplayName("[요청 금액만큼 메인 계좌에 입금한다.]")
	@Test
	void deposit() {
		// given
		User owner = UserFixture.basicUser();
		Account mainAccount = AccountFixture.accountWithTypeAndAmount(owner, MAIN_ACCOUNT, 300_000);
		SendToOthersRequestDto requestDto = new SendToOthersRequestDto(
			mainAccount.getId(),
			100_000
		);

		given(accountRepository.findById(mainAccount.getId()))
			.willReturn(Optional.of(mainAccount));

		// when
		accountService.deposit(owner.getId(), 200_000, requestDto);

		// then
		assertThat(mainAccount.getAmount()).isEqualTo(500_000);
	}

	@DisplayName("[요청 금액만큼 계좌에 입금할 때 오류가 발생하면 출금 취소 이벤트가 발행된다.]")
	@Test
	void depositThrowExceptionThenEventPublish() {
		// given
		User owner = UserFixture.basicUser();
		Account mainAccount = AccountFixture.accountWithTypeAndAmount(owner, SAVING_ACCOUNT, 300_000);
		SendToOthersRequestDto requestDto = new SendToOthersRequestDto(
			mainAccount.getId(),
			100_000
		);

		given(accountRepository.findById(any()))
			.willReturn(Optional.of(mainAccount));

		// when
		BaseException baseException = assertThrows(BaseException.class,
			() -> accountService.deposit(owner.getId(), 200_000, requestDto));

		// then
		verify(eventPublisher).publishEvent(any(WithdrawalFailEvent.class));
		assertThat(baseException.getMessage()).isEqualTo("해당 계좌에 입금을 실패했습니다.");
	}

	@DisplayName("[요청자의 계좌에서 요청 금액만큼 출금하고 출금 금액을 상대방 메인 계좌에 입금한다.]")
	@Test
	void sendToOthers() {
		// given
		User owner = UserFixture.basicUser();
		User others = UserFixture.basicUser();
		ReflectionTestUtils.setField(owner, "id", 1L);
		ReflectionTestUtils.setField(others, "id", 2L);

		Account mainAccountByOwner = AccountFixture.accountWithTypeAndAmount(owner, MAIN_ACCOUNT, 300_000);
		Account mainAccountByOthers = AccountFixture.accountWithTypeAndAmount(others, MAIN_ACCOUNT, 100_000);
		ReflectionTestUtils.setField(mainAccountByOwner, "id", 1L);
		ReflectionTestUtils.setField(mainAccountByOthers, "id", 2L);

		SendToOthersRequestDto requestDto = new SendToOthersRequestDto(
			mainAccountByOwner.getId(),
			100_000
		);

		given(accountRepository.findById(mainAccountByOwner.getId()))
			.willReturn(Optional.of(mainAccountByOwner));
		given(accountRepository.findById(mainAccountByOthers.getId()))
			.willReturn(Optional.of(mainAccountByOthers));

		// when
		accountService.sendToOthers(others.getId(), owner, requestDto);

		// then
		assertAll(
			() -> assertThat(mainAccountByOwner.getAmount()).isEqualTo(200_000),
			() -> assertThat(mainAccountByOthers.getAmount()).isEqualTo(200_000)
		);
	}
}
