package org.c4marathon.assignment.user.service;

import static org.assertj.core.api.Assertions.*;
import static org.c4marathon.assignment.user.domain.UserRole.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.c4marathon.assignment.common.exception.runtime.BaseException;
import org.c4marathon.assignment.common.jwt.JwtProvider;
import org.c4marathon.assignment.user.domain.User;
import org.c4marathon.assignment.user.dto.request.JoinRequestDto;
import org.c4marathon.assignment.user.dto.request.LoginRequestDto;
import org.c4marathon.assignment.user.dto.response.LoginResponseDto;
import org.c4marathon.assignment.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private JwtProvider jwtProvider;

	@InjectMocks
	private UserService userService;

	@DisplayName("회원가입 시 유저의 정보가 저장된다.")
	@Test
	void join() {
		// given
		JoinRequestDto request = new JoinRequestDto("abc@mini.com", "mini1234", "미니페이");
		given(userRepository.save(any(User.class))).willReturn(User.builder().email("abc@mini.com").build());

		// when
		userService.join(request);

		// then
		verify(userRepository).save(any(User.class));
	}

	@DisplayName("회원가입 정보와 일치하는 이메일과 비밀번호를 통해 로그인한다.")
	@Test
	void login() {
		// given
		LoginRequestDto request = new LoginRequestDto("abc@mini.com", "mini1234");
		User user = User.builder()
			.email("abc@mini.com")
			.name("김미니")
			.password("mini1234")
			.role(USER)
			.build();
		String expectedToken = "expectedToken123";

		given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));
		given(passwordEncoder.matches(any(), any())).willReturn(Boolean.TRUE);
		given(jwtProvider.createToken(any(), any())).willReturn(expectedToken);

		// when
		LoginResponseDto response = userService.login(request);

		// then
		assertThat(response.token()).isEqualTo(expectedToken);
	}

	@DisplayName("회원가입 정보와 이메일이 일치하는 유저가 없다면 로그인은 실패한다.")
	@Test
	void loginExceptionWithEmail() {
		// given
		LoginRequestDto request = new LoginRequestDto("abc@mini.com", "mini1234");

		given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());

		// when		// then
		assertThrows(BaseException.class, () -> userService.login(request));
	}

	@DisplayName("회원 비밀번호가 일치하지 않으면 로그인은 실패한다.")
	@Test
	void loginExceptionWithPassword() {
		// given
		LoginRequestDto request = new LoginRequestDto("abc@mini.com", "mini1234");
		User user = User.builder()
			.email("abc@mini.com")
			.name("김미니")
			.password("mini1234")
			.role(USER)
			.build();

		given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));
		given(passwordEncoder.matches(any(), any())).willReturn(Boolean.FALSE);

		// when		// then
		assertThrows(BaseException.class, () -> userService.login(request));
	}
}
