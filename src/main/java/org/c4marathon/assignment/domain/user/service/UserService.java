package org.c4marathon.assignment.domain.user.service;

import org.c4marathon.assignment.domain.user.dto.JoinResponseDto;
import org.c4marathon.assignment.domain.user.dto.LoginRequestDto;
import org.c4marathon.assignment.domain.user.dto.LoginResponseDto;
import org.c4marathon.assignment.domain.user.dto.UserDto;
import org.c4marathon.assignment.domain.user.dto.UserMapper;
import org.c4marathon.assignment.domain.user.entity.JoinResponseMsg;
import org.c4marathon.assignment.domain.user.entity.LoginResponseMsg;
import org.c4marathon.assignment.domain.user.entity.User;
import org.c4marathon.assignment.domain.user.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;
	private final ApplicationEventPublisher eventPublisher;

	//회원가입
	public JoinResponseDto join(UserDto userDto) {
		String userPhone = userDto.getUserPhone();
		if (duplicatedUser(userPhone)) {
			User user = UserMapper.toUser(userDto);
			userRepository.save(user);
			// eventPublisher.publishEvent(new ScheduleCreateEvent(user));

			JoinResponseDto joinResponseDto = JoinResponseDto.builder()
				.responseMsg(JoinResponseMsg.SUCCESS.getResponseMsg())
				.build();
			return joinResponseDto;
		} else {
			throw new HttpClientErrorException(HttpStatusCode.valueOf(400));
		}
	}

	//로그인
	public LoginResponseDto login(LoginRequestDto loginRequestDto) {
		if (userRepository.existsByUserPhone(loginRequestDto.getUserPhone())) {
			User user = userRepository.findByUserPhone(loginRequestDto.getUserPhone());
			if (loginRequestDto.getUserPassword().equals(user.getUserPassword())) {
				return LoginResponseDto.builder()
					.responseMsg(LoginResponseMsg.SUCCESS.getResponseMsg())
					.build();
			} else {
				return LoginResponseDto.builder()
					.responseMsg(LoginResponseMsg.FAIL.getResponseMsg())
					.build();
			}
		}
		throw new HttpClientErrorException(HttpStatusCode.valueOf(400));
	}

	private boolean duplicatedUser(String userPhone) {
		if (userRepository.existsByUserPhone(userPhone)) {
			return false;
		}
		return true;
	}
}
