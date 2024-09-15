package org.c4marathon.assignment.domain.user.service;

import static org.c4marathon.assignment.domain.user.entity.UserErrCode.*;

import org.c4marathon.assignment.domain.account.entity.ScheduleCreateEvent;
import org.c4marathon.assignment.domain.user.dto.JoinResponseDto;
import org.c4marathon.assignment.domain.user.dto.LoginRequestDto;
import org.c4marathon.assignment.domain.user.dto.LoginResponseDto;
import org.c4marathon.assignment.domain.user.dto.UserDto;
import org.c4marathon.assignment.domain.user.dto.UserMapper;
import org.c4marathon.assignment.domain.user.entity.JoinResponseMsg;
import org.c4marathon.assignment.domain.user.entity.LoginResponseMsg;
import org.c4marathon.assignment.domain.user.entity.User;
import org.c4marathon.assignment.domain.user.exception.UserException;
import org.c4marathon.assignment.domain.user.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
		String userPhone = userDto.getUserPhone();
		if (duplicatedUser(userPhone)) {
			throw new UserException(USER_DUPLICATED_FAIL);

		}
		User user = UserMapper.toUser(userDto, passwordEncoder);
		userRepository.save(user);
		eventPublisher.publishEvent(new ScheduleCreateEvent(user));

		return JoinResponseDto.builder()
			.responseMsg(JoinResponseMsg.SUCCESS.getResponseMsg())
			.build();
	}

	//로그인
	public LoginResponseDto login(LoginRequestDto loginRequestDto) {
		User user = userRepository.findByUserPhone(loginRequestDto.getUserPhone())
			.orElseThrow(() -> new UserException(USER_NOT_FOUND));
		if (!loginRequestDto.getUserPassword().equals(user.getUserPassword())) {
			throw new UserException(USER_LOGIN_FAIL);
		}
		return LoginResponseDto.builder()
			.responseMsg(LoginResponseMsg.SUCCESS.getResponseMsg())
			.build();
	}

	private boolean duplicatedUser(String userPhone) {
		return !userRepository.existsByUserPhone(userPhone);
	}
}
