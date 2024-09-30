package org.c4marathon.assignment.user.service;

import org.c4marathon.assignment.common.exception.BaseException;
import org.c4marathon.assignment.event.JoinEventDto;
import org.c4marathon.assignment.user.domain.User;
import org.c4marathon.assignment.user.dto.JoinDto;
import org.c4marathon.assignment.user.dto.LoginDto;
import org.c4marathon.assignment.user.exception.JoinException;
import org.c4marathon.assignment.user.exception.LoginException;
import org.c4marathon.assignment.user.exception.NotFoundException;
import org.c4marathon.assignment.user.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final ApplicationEventPublisher eventPublisher;

	// 회원가입
	@TransactionalEventListener
	public void save(JoinDto joinDto) {
		userRepository.findByUserId(joinDto.userId()).ifPresent(user -> {
			throw new BaseException(JoinException.FOUND_USER);
		});

		User user = User.builder()
			.userId(joinDto.userId())
			.userPw(joinDto.userPw())
			.name(joinDto.name())
			.build();

		userRepository.save(user);
		eventPublisher.publishEvent(new JoinEventDto(user));
	}

	// 로그인
	public LoginDto login(LoginDto loginDto) {
		User userEntity = userRepository.findByUserId(loginDto.userId())
			.orElseThrow(() -> new BaseException(NotFoundException.NOT_FOUND_USER));

		if (userEntity.getUserPw().equals(loginDto.userPw())) {
			return loginDto;
		} else {
			throw new BaseException(LoginException.NOT_MATCH_PASSWORD);
		}
	}
}
