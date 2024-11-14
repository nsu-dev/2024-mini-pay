package org.c4marathon.assignment.account.service;

import static org.assertj.core.api.Assertions.*;
import static org.c4marathon.assignment.account.domain.AccountType.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.c4marathon.assignment.account.domain.Account;
import org.c4marathon.assignment.account.dto.request.SendToOthersRequestDto;
import org.c4marathon.assignment.account.repository.AccountRepository;
import org.c4marathon.assignment.common.fixture.AccountFixture;
import org.c4marathon.assignment.common.fixture.UserFixture;
import org.c4marathon.assignment.user.domain.User;
import org.c4marathon.assignment.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;

@SpringBootTest
class BankingServiceTest {

	@Autowired
	private BankingService bankingService;

	@Autowired
	private AccountRepository accountRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@AfterEach
	void tearDown() {
		accountRepository.deleteAll();
		userRepository.deleteAll();
	}

	@DisplayName("[요청자의 계좌에서 요청 금액만큼 출금하고 출금 금액을 상대방 메인 계좌에 입금한다.]")
	@Test
	void sendToOthers() {
		// given
		User owner = UserFixture.basicUser();
		User others = UserFixture.others();
		userRepository.saveAll(List.of(owner, others));

		Account mainAccountByOwner = AccountFixture.accountWithTypeAndAmount(owner, MAIN_ACCOUNT, 300_000);
		Account mainAccountByOthers = AccountFixture.accountWithTypeAndAmount(others, MAIN_ACCOUNT, 100_000);
		accountRepository.saveAll(List.of(mainAccountByOwner, mainAccountByOthers));

		SendToOthersRequestDto requestDto = new SendToOthersRequestDto(
			mainAccountByOwner.getId(),
			100_000
		);

		// when
		bankingService.sendToOthers(mainAccountByOthers.getId(), owner, requestDto);
		Account updatedOwnerAccount = accountRepository.findById(mainAccountByOwner.getId()).orElseThrow();
		Account updatedOthersAccount = accountRepository.findById(mainAccountByOthers.getId()).orElseThrow();

		// then
		assertAll(
			() -> assertThat(updatedOwnerAccount.getAmount()).isEqualTo(200_000),
			() -> assertThat(updatedOthersAccount.getAmount()).isEqualTo(200_000)
		);
	}
}
