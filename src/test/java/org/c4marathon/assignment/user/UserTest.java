package org.c4marathon.assignment.user;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Optional;

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
	}

	@AfterEach
	void end() {
		userRepository.deleteAll();
	}

	@DisplayName("[회원가입 테스트]")
	@Test
	void joinTest() throws Exception {
		// given
		JoinDto joinDto = new JoinDto("aaaa", "ab12", "홍길동", 1234);

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
		given(userRepository.findByUserId("abcd")).willReturn(Optional.of(user));

		// when, then
		mockMvc.perform(post("/user/login")
				.content(toJson(loginDto))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk());
	}
}