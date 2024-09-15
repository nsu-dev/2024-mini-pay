package org.c4marathon.assignment.user.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.c4marathon.assignment.domain.user.dto.LoginRequestDto;
import org.c4marathon.assignment.domain.user.dto.LoginResponseDto;
import org.c4marathon.assignment.domain.user.dto.UserDto;
import org.c4marathon.assignment.domain.user.dto.UserMapper;
import org.c4marathon.assignment.domain.user.entity.LoginResponseMsg;
import org.c4marathon.assignment.domain.user.entity.User;
import org.c4marathon.assignment.domain.user.repository.UserRepository;
import org.c4marathon.assignment.domain.user.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
	@Mock
	private UserRepository userRepository;
	@InjectMocks
	private UserService userService;
	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private HttpServletRequest httpServletRequest;

	@Mock
	private HttpSession httpSession;
	@Mock
	private UserMapper userMapper;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		// Mocking HttpSession and HttpServletRequest
		given(httpServletRequest.getSession()).willReturn(httpSession);
		given(httpSession.getAttribute("userId")).willReturn(1L);
	}

	@AfterEach
	void tearDown() {
		userRepository.deleteAll();
	}

	@DisplayName("회원가입 시 회원 정보 저장")
	@Test
	void join() {
		//given
		UserDto userDto = new UserDto("010-8337-6023", "조아빈", "20000604", "pw123");
		given(userRepository.save(any(User.class))).willReturn(User.builder().userPhone("010-8337-6023").build());

		//when
		userService.join(userDto);

		//then
		verify(userRepository).save(any(User.class));
	}

	@DisplayName("로그인 시 일치하는 정보가 있다면 로그인은 성공한다.")
	@Test
	void login() {
		// given
		LoginRequestDto loginRequestDto = new LoginRequestDto("010-8337-6023", "pw123");
		User mockUser = User.builder()
			.userPhone("010-8337-6023")
			.userPassword(passwordEncoder.encode("pw123"))
			.build();

		// Mock userRepository and passwordEncoder
		given(userRepository.findByUserPhone(anyString())).willReturn(Optional.of(mockUser));
		given(passwordEncoder.matches(any(), any())).willReturn(Boolean.TRUE);

		// Mock HttpServletRequest and HttpSession
		HttpSession mockSession = mock(HttpSession.class);
		given(httpServletRequest.getSession()).willReturn(mockSession);
		given(httpServletRequest.getSession(true)).willReturn(mockSession);

		// when
		LoginResponseDto responseDto = userService.login(loginRequestDto, httpServletRequest);

		// then
		assertThat(responseDto.responseMsg()).isEqualTo(LoginResponseMsg.SUCCESS.getResponseMsg());

		// Verify HttpSession methods are called
		verify(mockSession).setAttribute("userId", mockUser.getUserId());
		verify(mockSession).setMaxInactiveInterval(1800);
		verify(mockSession).invalidate();
	}
}
