package org.c4marathon.assignment.user.service;

import java.util.Date;

import org.c4marathon.assignment.common.exception.runtime.BaseException;
import org.c4marathon.assignment.common.jwt.JwtProvider;
import org.c4marathon.assignment.event.account.AccountEvent;
import org.c4marathon.assignment.user.domain.User;
import org.c4marathon.assignment.user.dto.UserMapper;
import org.c4marathon.assignment.user.dto.request.JoinRequestDto;
import org.c4marathon.assignment.user.dto.request.LoginRequestDto;
import org.c4marathon.assignment.user.dto.response.JoinResponseDto;
import org.c4marathon.assignment.user.dto.response.LoginResponseDto;
import org.c4marathon.assignment.user.exception.UserErrorCode;
import org.c4marathon.assignment.user.repository.UserRepository;
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
	private final JwtProvider jwtProvider;

	@Transactional
	public JoinResponseDto join(JoinRequestDto joinRequestDto) {

		if (checkDuplicatedEmail(joinRequestDto.email())) {
			throw new BaseException(UserErrorCode.DUPLICATED_EMAIL);
		}

		User joinUser = UserMapper.toUserFromJoinRequestDto(joinRequestDto, passwordEncoder);
		User savedUser = userRepository.save(joinUser);

		eventPublisher.publishEvent(new AccountEvent(savedUser));

		return UserMapper.toJoinResponseDtoFromUser(savedUser);
	}

	private boolean checkDuplicatedEmail(String email) {
		return userRepository.existsByEmail(email);
	}

	public LoginResponseDto login(LoginRequestDto loginRequestDto) {
		User findUser = userRepository.findByEmail(loginRequestDto.email())
			.orElseThrow(() -> new BaseException(UserErrorCode.NOT_FOUND_USER));

		if (!passwordEncoder.matches(loginRequestDto.password(), findUser.getPassword())) {
			new BaseException(UserErrorCode.NOT_MATCH_PASSWORD);
		}

		String token = jwtProvider.createToken(findUser, new Date());

		return UserMapper.toLoginResponseDto(token);
	}
}
