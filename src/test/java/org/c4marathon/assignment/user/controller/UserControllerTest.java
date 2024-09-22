package org.c4marathon.assignment.user.controller;

import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.c4marathon.assignment.common.support.ApiTestSupport;
import org.c4marathon.assignment.user.dto.request.JoinRequestDto;
import org.c4marathon.assignment.user.dto.request.LoginRequestDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserControllerTest extends ApiTestSupport {

	@AfterEach
	void tearDown() {
		userRepository.deleteAll();
		accountRepository.deleteAll();
	}

	@DisplayName("[회원가입 테스트]")
	@Test
	void joinApi() throws Exception {
		// given
		JoinRequestDto request = new JoinRequestDto("join@mini.com", "1234567", "김미니");

		// when		// then
		mockMvc.perform(post("/api/user/join")
				.content(toJson(request))
				.contentType(APPLICATION_JSON)
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.email").value("join@mini.com"));
	}

	@DisplayName("[로그인 테스트]")
	@Test
	void loginApi() throws Exception {
		// given
		final String password = "mini1234";
		LoginRequestDto request = new LoginRequestDto(loginUser.getEmail(), password);

		// when		// then
		mockMvc.perform(post("/api/user/login")
				.content(toJson(request))
				.contentType(APPLICATION_JSON)
			)
			.andExpect(status().isOk());
	}
}
