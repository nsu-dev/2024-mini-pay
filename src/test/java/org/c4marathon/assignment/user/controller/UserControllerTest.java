package org.c4marathon.assignment.user.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.c4marathon.assignment.domain.user.dto.JoinResponseDto;
import org.c4marathon.assignment.domain.user.dto.LoginRequestDto;
import org.c4marathon.assignment.domain.user.dto.UserDto;
import org.c4marathon.assignment.domain.user.entity.JoinResponseMsg;
import org.c4marathon.assignment.domain.user.entity.UserErrCode;
import org.c4marathon.assignment.domain.user.exception.UserException;
import org.c4marathon.assignment.domain.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@MockBean
	private UserService userService; // UserService를 @MockBean으로 설정

	@Test
	@DisplayName("회원가입 API 테스트")
	void userJoin() throws Exception {
		// given
		UserDto userDto = new UserDto("010-8337-6023", "조아빈", "20000604", "pw123");
		JoinResponseDto joinResponseDto = new JoinResponseDto(JoinResponseMsg.SUCCESS.getResponseMsg());

		// userService.join 호출 시 응답값을 미리 지정
		given(userService.join(any(UserDto.class))).willReturn(joinResponseDto);

		// when & then
		mockMvc.perform(post("/user/join")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(userDto)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.responseMsg").value(JoinResponseMsg.SUCCESS.getResponseMsg())); // 응답 메시지를 확인
	}

	@Test
	@DisplayName("로그인 api 테스트")
	void userLogin() throws Exception{
		//given
		LoginRequestDto loginRequestDto = new LoginRequestDto("010-8337-6023", "pw123");

		//when   //then
		mockMvc.perform(post("/user/login")
			.contentType(MediaType.APPLICATION_JSON)
			.content(new ObjectMapper().writeValueAsString(loginRequestDto))
		).andExpect(status().isOk());
	}
	@DisplayName("로그인 시 전화번호나 비밀번호가 공백이면 예외가 발생한다.")
	@Test
	void loginValidationErr() throws Exception{
		//given
		LoginRequestDto loginRequestDto = new LoginRequestDto("", "pw111");
		//when   //then
		mockMvc.perform(post("/user/login")
			.contentType(MediaType.APPLICATION_JSON)
			.content(new ObjectMapper().writeValueAsString(loginRequestDto))
		).andExpect(status().isBadRequest());
	}
	@Test
	@DisplayName("로그인 시 런타임 에러가 발생한다면 정상적으로 처리한다.")
	void loginRuntimeException() throws Exception{
		//given
		LoginRequestDto loginRequestDto = new LoginRequestDto("010-8337-6023", "pw111");

		given(userService.login(any(LoginRequestDto.class), any())).willThrow(new RuntimeException());

		//when //then
		mockMvc.perform(post("/user/login")
			.contentType(MediaType.APPLICATION_JSON)
			.content(new ObjectMapper().writeValueAsString(loginRequestDto))
		).andExpect(status().isInternalServerError());
	}

	@Test
	@DisplayName("회원가입 시 런타임 에러가 발생한다면 정상적으로 처리한다.")
	void joinRuntimeException() throws Exception{
		//given
		UserDto userDto = new UserDto("010-8337-6023", "조아빈", "20000604", "pw123");
		given(userService.join(any(UserDto.class))).willThrow(new RuntimeException());

		//when //then
		mockMvc.perform(post("/user/join")
			.contentType(MediaType.APPLICATION_JSON)
			.content(new ObjectMapper().writeValueAsString(userDto))
		).andExpect(status().isInternalServerError());
	}

	@Test
	@DisplayName("회원가입 시 공백인 요소가 있다면 예외가 발생하고 정상적으로 처리한다.")
	void joinValidationErr() throws Exception{
		//given
		UserDto userDto = new UserDto("", "조아빈", "20000604", "pw123");

		//when //then
		mockMvc.perform(post("/user/join")
			.contentType(MediaType.APPLICATION_JSON)
			.content(new ObjectMapper().writeValueAsString(userDto))
		).andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("로그인 시 전화번호나 비밀번호가 일치하지 않으면 예외가 발생하고 정상적으로 처리한다.")
	void loginInvalidErr() throws Exception{
		//given
		LoginRequestDto loginRequestDto = new LoginRequestDto("010-8337-6024", "pw111");
		given(userService.login(any(LoginRequestDto.class), any())).willThrow(new UserException(UserErrCode.USER_INVALID_FAIL));
		//when //then
		mockMvc.perform(post("/user/login")
			.contentType(MediaType.APPLICATION_JSON)
			.content(new ObjectMapper().writeValueAsString(loginRequestDto))
		).andExpect(status().isBadRequest());
	}
}
