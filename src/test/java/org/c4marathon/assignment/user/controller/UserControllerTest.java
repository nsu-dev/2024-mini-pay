package org.c4marathon.assignment.user.controller;

import static org.c4marathon.assignment.user.domain.UserRole.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.c4marathon.assignment.account.repository.AccountRepository;
import org.c4marathon.assignment.user.domain.User;
import org.c4marathon.assignment.user.dto.request.JoinRequestDto;
import org.c4marathon.assignment.user.dto.request.LoginRequestDto;
import org.c4marathon.assignment.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private AccountRepository accountRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

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
				.content(objectMapper.writeValueAsString(request))
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
		User user = User.builder()
			.email("abc@mini.com")
			.name("김미니")
			.password(passwordEncoder.encode(password))
			.role(USER)
			.build();
		userRepository.save(user);
		LoginRequestDto request = new LoginRequestDto(user.getEmail(), password);

		// when		// then
		mockMvc.perform(post("/api/user/login")
				.content(objectMapper.writeValueAsString(request))
				.contentType(APPLICATION_JSON)
			)
			.andExpect(status().isOk());
	}
}
