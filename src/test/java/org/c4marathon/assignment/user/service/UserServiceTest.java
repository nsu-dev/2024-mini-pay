package org.c4marathon.assignment.user.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.c4marathon.assignment.domain.user.dto.response.JoinResponseDto;
import org.c4marathon.assignment.domain.user.dto.request.LoginRequestDto;
import org.c4marathon.assignment.domain.user.dto.response.LoginResponseDto;
import org.c4marathon.assignment.domain.user.dto.request.UserDto;
import org.c4marathon.assignment.domain.user.entity.responsemsg.JoinResponseMsg;
import org.c4marathon.assignment.domain.user.entity.responsemsg.LoginResponseMsg;
import org.c4marathon.assignment.domain.user.entity.user.User;
import org.c4marathon.assignment.domain.user.entity.responsemsg.UserErrCode;
import org.c4marathon.assignment.domain.user.exception.UserException;
import org.c4marathon.assignment.domain.user.repository.UserRepository;
import org.c4marathon.assignment.domain.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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

	@BeforeEach
	void setup() {
		userRepository.deleteAll();
	}

	@DisplayName("회원가입 시 회원 정보 저장")
	@Test
	void join() {
		// given
		UserDto userDto = new UserDto("010-8337-6023", "조아빈", "20000604", "pw123");
		User mockUser = User.builder().userPhone("010-8337-6023").build();
		given(userRepository.save(any(User.class))).willReturn(mockUser);

		// when
		JoinResponseDto joinResponseDto = userService.join(userDto);

		// then
		verify(userRepository).save(any(User.class)); // userRepository의 save 메소드가 호출되었는지 확인
		assertThat(joinResponseDto.responseMsg()).isEqualTo(JoinResponseMsg.SUCCESS.getResponseMsg()); // 반환된 결과 검증
	}

	@DisplayName("회원가입 시 회원 정보가 중복이라면 예외가 발생한다.")
	@Test
	void joinDuplicatedErr() {
		// given
		UserDto userDto = new UserDto("010-8337-6023", "조아빈", "20000604", "pw123");
		given(userRepository.existsByUserPhone(userDto.userPhone())).willReturn(true);
		// when //then
		UserException exception = assertThrows(UserException.class, () -> userService.join(userDto)); // 반환된 결과 검증
		assertEquals(exception.getUserErrCode().getMessage(), UserErrCode.USER_DUPLICATED_FAIL.getMessage());
	}

	@DisplayName("로그인 시 일치하는 정보가 있다면 로그인은 성공하고 세션이 등록된다.")
	@Test
	void login() {
		// given
		LoginRequestDto loginRequestDto = new LoginRequestDto("010-8337-6023", "pw123");
		User mockUser = User.builder()
			.userPhone("010-8337-6023")
			.userPassword(passwordEncoder.encode("pw123"))
			.build();
		given(httpServletRequest.getSession(true)).willReturn(httpSession);
		given(httpServletRequest.getSession()).willReturn(httpSession);
		given(userRepository.findByUserPhone(anyString())).willReturn(Optional.of(mockUser));
		given(passwordEncoder.matches(any(), any())).willReturn(Boolean.TRUE);

		given(httpServletRequest.getSession()).willReturn(httpSession);

		// when
		LoginResponseDto responseDto = userService.login(loginRequestDto, httpServletRequest);

		// then
		assertThat(responseDto.responseMsg()).isEqualTo(LoginResponseMsg.SUCCESS.getResponseMsg());
		verify(httpSession).setAttribute("userId", mockUser.getUserId());
		verify(httpSession).setMaxInactiveInterval(1800);
	}

	@DisplayName("로그인 시 일치하는 전화번호가 없다면 로그인 실패 예외가 발생한다.")
	@Test
	void loginInvalidUserPhone() {
		//given
		LoginRequestDto loginRequestDto = new LoginRequestDto("010-8337-6024", "pw123");
		given(userRepository.findByUserPhone(anyString())).willReturn(Optional.empty());

		//when //then
		UserException exception = assertThrows(UserException.class,
			() -> userService.login(loginRequestDto, httpServletRequest));
		assertEquals(exception.getUserErrCode().getMessage(), UserErrCode.USER_NOT_FOUND.getMessage());
	}

	@DisplayName("로그인 시 비밀번호가 일치하지 않다면 로그인 실패 예외가 발생한다.")
	@Test
	void loginInvalidUserPassword() {
		//given
		LoginRequestDto loginRequestDto = new LoginRequestDto("010-8337-6023", "pw111");
		User mockUser = User.builder()
			.userPhone("010-8337-6023")
			.userPassword(passwordEncoder.encode("pw123"))
			.build();

		given(userRepository.findByUserPhone(anyString())).willReturn(Optional.of(mockUser));
		given(passwordEncoder.matches(any(), any())).willReturn(Boolean.FALSE);

		// when //then
		UserException exception = assertThrows(UserException.class,
			() -> userService.login(loginRequestDto, httpServletRequest));
		assertEquals(exception.getUserErrCode().getMessage(), UserErrCode.USER_LOGIN_FAIL.getMessage());
	}
}
