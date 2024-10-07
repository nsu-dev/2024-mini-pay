package org.c4marathon.assignment.domain.user.service;

import static org.c4marathon.assignment.domain.user.entity.responsemsg.UserErrCode.*;

import org.c4marathon.assignment.domain.account.entity.ScheduleCreateEvent;
import org.c4marathon.assignment.domain.user.dto.response.JoinResponseDto;
import org.c4marathon.assignment.domain.user.dto.request.LoginRequestDto;
import org.c4marathon.assignment.domain.user.dto.response.LoginResponseDto;
import org.c4marathon.assignment.domain.user.dto.request.UserDto;
import org.c4marathon.assignment.domain.user.dto.UserMapper;
import org.c4marathon.assignment.domain.user.entity.responsemsg.JoinResponseMsg;
import org.c4marathon.assignment.domain.user.entity.responsemsg.LoginResponseMsg;
import org.c4marathon.assignment.domain.user.entity.user.User;
import org.c4marathon.assignment.domain.user.exception.UserException;
import org.c4marathon.assignment.domain.user.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;
	private final ApplicationEventPublisher eventPublisher;
	private final PasswordEncoder passwordEncoder;

	//회원가입
	@Transactional
	public JoinResponseDto join(UserDto userDto) {
		String userPhone = userDto.userPhone();
		if (duplicatedUser(userPhone)) {
			throw new UserException(USER_DUPLICATED_FAIL);
		}
		User user = UserMapper.toUser(userDto, passwordEncoder);
		userRepository.save(user);
		eventPublisher.publishEvent(new ScheduleCreateEvent(user));

		return new JoinResponseDto(JoinResponseMsg.SUCCESS.getResponseMsg());
	}

	//로그인
	public LoginResponseDto login(LoginRequestDto loginRequestDto, HttpServletRequest httpServletRequest) {
		User user = userRepository.findByUserPhone(loginRequestDto.userPhone())
			.orElseThrow(() -> new UserException(USER_NOT_FOUND));
		if (!passwordEncoder.matches(loginRequestDto.userPassword(), user.getUserPassword())) {
			throw new UserException(USER_LOGIN_FAIL);
		}
		registerSession(user, httpServletRequest);
		return new LoginResponseDto(LoginResponseMsg.SUCCESS.getResponseMsg());
	}

	//로그인 한 사용자 세션 등록
	private void registerSession(User user, HttpServletRequest httpServletRequest) {
		httpServletRequest.getSession().invalidate();
		HttpSession session = httpServletRequest.getSession(true);
		session.setAttribute("userId", user.getUserId());
		session.setMaxInactiveInterval(1800); //30분
	}

	private boolean duplicatedUser(String userPhone) {
		return userRepository.existsByUserPhone(userPhone);
	}
}
