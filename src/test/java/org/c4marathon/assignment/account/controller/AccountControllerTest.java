package org.c4marathon.assignment.account.controller;

import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.c4marathon.assignment.account.domain.AccountType;
import org.c4marathon.assignment.common.support.ApiTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AccountControllerTest extends ApiTestSupport {

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
			.andExpect(jsonPath("$.userEmail").value(loginUser.getEmail()));
	}
}
