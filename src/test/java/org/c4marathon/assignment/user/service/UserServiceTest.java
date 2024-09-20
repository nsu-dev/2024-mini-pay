package org.c4marathon.assignment.user.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.c4marathon.assignment.common.exception.runtime.BaseException;
import org.c4marathon.assignment.common.fixture.UserFixture;
import org.c4marathon.assignment.common.jwt.JwtProvider;
import org.c4marathon.assignment.user.domain.User;
import org.c4marathon.assignment.user.dto.request.JoinRequestDto;
import org.c4marathon.assignment.user.dto.request.LoginRequestDto;
import org.c4marathon.assignment.user.dto.response.LoginResponseDto;
import org.c4marathon.assignment.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private JwtProvider jwtProvider;

	@Mock
	private ApplicationEventPublisher eventPublisher;

	@InjectMocks
	private UserService userService;

	@AfterEach
	void tearDown() {
		userRepository.deleteAll();
	}

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

	@DisplayName("회원가입 시 중복된 이메일이 존재한다면 예외가 발생한다.")
	@Test
	void joinWithDuplicatedEmail() {
		// given
		JoinRequestDto request = new JoinRequestDto("abc@mini.com", "mini1234", "미니페이");
		given(userRepository.existsByEmail(anyString())).willReturn(Boolean.TRUE);

		// when
		BaseException baseException = assertThrows(BaseException.class, () -> userService.join(request));

		// then
		assertThat(baseException.getMessage()).isEqualTo("중복된 이메일 회원이 존재합니다.");
	}

	@DisplayName("회원가입 정보와 일치하는 이메일과 비밀번호를 통해 로그인한다.")
	@Test
	void login() {
		// given
		LoginRequestDto request = new LoginRequestDto("abc@mini.com", "mini1234");
		User user = UserFixture.basicUser();
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
		User user = UserFixture.basicUser();

		given(userRepository.findByEmail(anyString())).willReturn(Optional.of(user));
		given(passwordEncoder.matches(any(), any())).willReturn(Boolean.FALSE);

		// when		// then
		assertThrows(BaseException.class, () -> userService.login(request));
	}
}
