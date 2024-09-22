package org.c4marathon.assignment.account.controller;

import static org.c4marathon.assignment.account.domain.AccountType.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.c4marathon.assignment.account.domain.Account;
import org.c4marathon.assignment.account.domain.AccountType;
import org.c4marathon.assignment.account.dto.request.ChargeRequestDto;
import org.c4marathon.assignment.account.dto.request.SendRequestDto;
import org.c4marathon.assignment.account.dto.request.SendToOthersRequestDto;
import org.c4marathon.assignment.common.fixture.AccountFixture;
import org.c4marathon.assignment.common.fixture.UserFixture;
import org.c4marathon.assignment.common.support.ApiTestSupport;
import org.c4marathon.assignment.user.domain.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AccountControllerTest extends ApiTestSupport {
	@AfterEach
	void tearDown() {
		accountRepository.deleteAll();
	}

	@DisplayName("[적금계좌를 생성한다]")
	@Test
	void generateSavingAccount() throws Exception {
		// given

		// when		// then
		mockMvc.perform(post("/api/user/saving-account")
				.header("Authorization", "Bearer " + token)
				.contentType(APPLICATION_JSON)
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.type").value(AccountType.SAVING_ACCOUNT.getType()))
			.andExpect(jsonPath("$.amount").value(0))
			.andExpect(jsonPath("$.userName").value(loginUser.getName()))
			.andExpect(jsonPath("$.userEmail").value(loginUser.getEmail())
			);
	}

	@DisplayName("[메인계좌에서 적금계좌로 돈을 송금한다.]")
	@Test
	void sendMoney() throws Exception {
		// given
		Account mainAccount = AccountFixture.accountWithTypeAndAmount(loginUser, MAIN_ACCOUNT, 600_000);
		Account savingAccount = AccountFixture.accountWithTypeAndAmount(loginUser, SAVING_ACCOUNT, 0);
		accountRepository.saveAll(List.of(mainAccount, savingAccount));

		SendRequestDto requestDto = new SendRequestDto(
			mainAccount.getId(),
			mainAccount.getType().getType(),
			300_000,
			savingAccount.getId(),
			savingAccount.getType().getType()
		);

		// when		// then
		mockMvc.perform(post("/api/send")
				.header("Authorization", "Bearer " + token)
				.content(toJson(requestDto))
				.contentType(APPLICATION_JSON)
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.toAccountId").value(mainAccount.getId()))
			.andExpect(jsonPath("$.toAccountType").value(mainAccount.getType().getType()))
			.andExpect(jsonPath("$.toAccountMoney").value(mainAccount.getAmount() - 300_000))
			.andExpect(jsonPath("$.fromAccountId").value(savingAccount.getId()))
			.andExpect(jsonPath("$.fromAccountType").value(savingAccount.getType().getType()))
			.andExpect(jsonPath("$.fromAccountMoney").value(savingAccount.getAmount() + 300_000)
			);
	}

	@DisplayName("[자신의 모든계좌를 조회한다.]")
	@Test
	void getAccounts() throws Exception {
		// given
		Account mainAccount = AccountFixture.accountWithTypeAndAmount(loginUser, MAIN_ACCOUNT, 600_000);
		Account savingAccount = AccountFixture.accountWithTypeAndAmount(loginUser, SAVING_ACCOUNT, 0);
		accountRepository.saveAll(List.of(mainAccount, savingAccount));

		// when		// then
		mockMvc.perform(get("/api/accounts")
				.header("Authorization", "Bearer " + token)
				.contentType(APPLICATION_JSON)
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.size()").value(2))
			.andExpect(jsonPath("$.[0].id").value(mainAccount.getId()))
			.andExpect(jsonPath("$.[0].type").value(mainAccount.getType().getType()))
			.andExpect(jsonPath("$.[0].amount").value(mainAccount.getAmount()))
			.andExpect(jsonPath("$.[0].limitAmount").value(mainAccount.getLimitAmount()))
			.andExpect(jsonPath("$.[1].id").value(savingAccount.getId()))
			.andExpect(jsonPath("$.[1].type").value(savingAccount.getType().getType()))
			.andExpect(jsonPath("$.[1].amount").value(savingAccount.getAmount()))
			.andExpect(jsonPath("$.[1].limitAmount").value(savingAccount.getLimitAmount())
			);
	}

	@DisplayName("[메인계좌의 충전한도 한에서 금액을 충전한다.]")
	@Test
	void chargeMainAccount() throws Exception {
		// given
		Account mainAccount = AccountFixture.accountWithTypeAndAmount(loginUser, MAIN_ACCOUNT, 300_000);
		accountRepository.save(mainAccount);

		ChargeRequestDto requestDto = new ChargeRequestDto(mainAccount.getId(), 300_000);

		// when		// then
		mockMvc.perform(post("/api/account/charge")
				.header("Authorization", "Bearer " + token)
				.content(toJson(requestDto))
				.contentType(APPLICATION_JSON)
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.accountId").value(mainAccount.getId()))
			.andExpect(jsonPath("$.amount").value(mainAccount.getAmount() + 300_000))
			.andExpect(jsonPath("$.limitAmount").value(mainAccount.getLimitAmount() - 300_000)
			);
	}

	@DisplayName("[유저 메인계좌 간 송금한다.]")
	@Test
	void sendToOthers() throws Exception {
		// given
		final int sendToAmount = 100_000;

		User others = UserFixture.others();
		userRepository.save(others);

		Account mainAccount1 = AccountFixture.accountWithTypeAndAmount(loginUser, MAIN_ACCOUNT, 300_000);
		Account mainAccount2 = AccountFixture.accountWithTypeAndAmount(others, MAIN_ACCOUNT, 200_000);
		accountRepository.saveAll(List.of(mainAccount1, mainAccount2));

		SendToOthersRequestDto requestDto = new SendToOthersRequestDto(
			mainAccount1.getId(),
			MAIN_ACCOUNT.getType(),
			sendToAmount
		);

		// when		// then
		mockMvc.perform(
				post("/api/send/{othersAccountId}/{othersAccountType}", mainAccount2.getId(), MAIN_ACCOUNT.getType())
					.header("Authorization", "Bearer " + token)
					.content(toJson(requestDto))
					.contentType(APPLICATION_JSON)
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.sendFromUserName").value(others.getName()))
			.andExpect(jsonPath("$.amount").value(sendToAmount)
			);
	}
}
