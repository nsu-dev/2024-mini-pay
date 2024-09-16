package org.c4marathon.assignment.user.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.c4marathon.assignment.domain.user.controller.UserController;
import org.c4marathon.assignment.domain.user.dto.JoinResponseDto;
import org.c4marathon.assignment.domain.user.dto.LoginRequestDto;
import org.c4marathon.assignment.domain.user.dto.LoginResponseDto;
import org.c4marathon.assignment.domain.user.dto.UserDto;
import org.c4marathon.assignment.domain.user.entity.JoinResponseMsg;
import org.c4marathon.assignment.domain.user.entity.User;
import org.c4marathon.assignment.domain.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private UserService userService; // UserService를 @MockBean으로 설정

	@Mock
	PasswordEncoder passwordEncoder;
	@Test
	@DisplayName("회원가입 API 테스트")
	void userJoin() throws Exception {
		// given
		UserDto userDto = new UserDto("010-8337-6023", "조아빈", "20000604", "pw123");
		JoinResponseDto joinResponseDto = new JoinResponseDto(JoinResponseMsg.SUCCESS.getResponseMsg());

		// userService.join 호출 시 응답값을 미리 지정
		BDDMockito.given(userService.join(BDDMockito.any(UserDto.class))).willReturn(joinResponseDto);

		// when & then
		mockMvc.perform(post("/user/join")
				.contentType(MediaType.APPLICATION_JSON)
				.content(new ObjectMapper().writeValueAsString(userDto))
			)
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
}
