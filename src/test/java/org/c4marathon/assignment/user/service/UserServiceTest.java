package org.c4marathon.assignment.user.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.c4marathon.assignment.domain.user.dto.JoinResponseDto;
import org.c4marathon.assignment.domain.user.dto.LoginRequestDto;
import org.c4marathon.assignment.domain.user.dto.LoginResponseDto;
import org.c4marathon.assignment.domain.user.dto.UserDto;
import org.c4marathon.assignment.domain.user.dto.UserErrDto;
import org.c4marathon.assignment.domain.user.entity.JoinResponseMsg;
import org.c4marathon.assignment.domain.user.entity.LoginResponseMsg;
import org.c4marathon.assignment.domain.user.entity.User;
import org.c4marathon.assignment.domain.user.entity.UserErrCode;
import org.c4marathon.assignment.domain.user.exception.UserException;
import org.c4marathon.assignment.domain.user.exception.UserExceptionHandler;
import org.c4marathon.assignment.domain.user.repository.UserRepository;
import org.c4marathon.assignment.domain.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
	private UserErrCode mockUserErrCode;
	@InjectMocks
	private UserExceptionHandler userExceptionHandler;

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
		assertThrows(UserException.class, () -> userService.join(userDto)); // 반환된 결과 검증
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
		assertThrows(UserException.class, () -> userService.login(loginRequestDto, httpServletRequest));
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
		assertThrows(UserException.class, () -> userService.login(loginRequestDto, httpServletRequest));
	}

	@DisplayName("로그인 시 예외에서 예외코드가 비정상적인 코드라도 예외를 정상적으로 처리한다.")
	@Test
	public void testHandleIllegalAccessError() {
		// UserErrCode.getStatus()가 잘못된 값을 반환하도록 설정하여 IllegalAccessError 발생 시뮬레이션
		given(mockUserErrCode.getStatus()).willReturn(9999); // 유효하지 않은 HTTP 상태 코드

		// getUserErrDto 호출 시 예외가 발생하는지 테스트
		ResponseEntity<UserErrDto> response = userExceptionHandler.getUserErrDto(mockUserErrCode);

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
	}
}
