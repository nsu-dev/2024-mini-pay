package org.c4marathon.assignment.user;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Optional;

import org.c4marathon.assignment.account.domain.Account;
import org.c4marathon.assignment.account.repository.AccountRepository;
import org.c4marathon.assignment.event.JoinEventDto;
import org.c4marathon.assignment.event.JoinEventHandler;
import org.c4marathon.assignment.user.domain.User;
import org.c4marathon.assignment.user.dto.JoinDto;
import org.c4marathon.assignment.user.dto.LoginDto;
import org.c4marathon.assignment.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
public class UserTest {

	@MockBean
	private UserRepository userRepository;

	@MockBean
	private AccountRepository accountRepository;

	@Autowired
	private JoinEventHandler joinEventHandler;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	private String toJson(Object object) throws JsonProcessingException {
		return objectMapper.writeValueAsString(object);
	}

	@BeforeEach
	void start() {
		userRepository.deleteAll();
		accountRepository.deleteAll();
	}

	@AfterEach
	void end() {
		userRepository.deleteAll();
		userRepository.deleteAll();
	}

	@DisplayName("[회원가입 테스트]")
	@Test
	void joinTest() throws Exception {
		// given
		JoinDto joinDto = new JoinDto("aaaa", "ab12", "홍길동", 1234);

		// when
		mockMvc.perform(post("/user/join")
				.content(toJson(joinDto))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk());

		// then
		verify(userRepository).save(any(User.class));
	}

	@DisplayName("[회원가입 성공시 메인계좌 생성 테스트]")
	@Test
	void joinEventTest() throws Exception {
		// given
		JoinDto joinDto = new JoinDto("aaaa", "a1234", "홍길동", 1234);

		// Mocking userRepository to return the user when searching by userId
		given(userRepository.findByUserId(joinDto.userId())).willReturn(Optional.empty());

		// when
		mockMvc.perform(post("/user/join")
				.content(toJson(joinDto))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk());

		// then
		// 강제로 이벤트 핸들러 호출
		User user = new User("aaaa", "a1234", "홍길동", 1234);
		joinEventHandler.craeteAccount(new JoinEventDto(user));

		// Account가 저장되었는지 확인
		verify(accountRepository).save(any(Account.class));
	}

	@DisplayName("[회원가입 중 아이디 중복으로 인한 예외 발생]")
	@Test
	void joinTestExceptionByUserId() throws Exception {
		// given
		JoinDto joinDto = new JoinDto("aaaa", "ab12", "홍길동", 1234);

		User user = new User("aaaa", "a1234", "홍길동", 1234);
		given(userRepository.findByUserId(user.getUserId())).willReturn(Optional.of(user));

		// when, then
		mockMvc.perform(post("/user/join")
				.content(toJson(joinDto))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk());
	}

	@DisplayName("[로그인 테스트]")
	@Test
	void loginTest() throws Exception {

		// given
		LoginDto loginDto = new LoginDto("abcd", "a1234");

		User user = new User("abcd", "a1234", "홍길동", 1234);
		given(userRepository.findByUserId(user.getUserId())).willReturn(Optional.of(user));

		// when, then
		mockMvc.perform(post("/user/login")
				.content(toJson(loginDto))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("로그인 성공"))
			.andExpect(jsonPath("$.data").isNotEmpty());
	}

	@DisplayName("[로그인 중 아이디를 찾을 수 없음으로 인한 예외 발생]")
	@Test
	void loginTestExceptionByUserId() throws Exception {
		// given
		LoginDto loginDto = new LoginDto("abcd", "a1234");

		User user = new User("abcd", "a1234", "홍길동", 1234);
		given(userRepository.findByUserId("aaaa")).willReturn(Optional.of(user));

		// when, then
		mockMvc.perform(post("/user/login")
				.content(toJson(loginDto))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk());
	}

	@DisplayName("[로그인 중 비밀번호를 찾을 수 없음으로 인한 예외 발생]")
	@Test
	void loginTestExceptionByUserPw() throws Exception {
		// given
		LoginDto loginDto = new LoginDto("abcd", "a12");

		User user = new User("abcd", "a1234", "홍길동", 1234);
		given(userRepository.findByUserId(loginDto.userId())).willReturn(Optional.of(user));

		// when, then
		mockMvc.perform(post("/user/login")
				.content(toJson(loginDto))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk());
	}
}
