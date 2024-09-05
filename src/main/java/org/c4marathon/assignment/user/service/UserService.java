package org.c4marathon.assignment.user.service;

import org.c4marathon.assignment.common.exception.runtime.BaseException;
import org.c4marathon.assignment.user.domain.User;
import org.c4marathon.assignment.user.dto.UserMapper;
import org.c4marathon.assignment.user.dto.request.JoinRequestDto;
import org.c4marathon.assignment.user.dto.response.JoinResponseDto;
import org.c4marathon.assignment.user.exception.UserErrorCode;
import org.c4marathon.assignment.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;

	@Transactional
	public JoinResponseDto join(JoinRequestDto joinRequestDto) {

		if (checkDuplicatedEmail(joinRequestDto.email())) {
			throw new BaseException(UserErrorCode.DUPLICATED_EMAIL);
		}

		User joinUser = UserMapper.toUserFromJoinRequestDto(joinRequestDto);
		User savedUser = userRepository.save(joinUser);

		return UserMapper.toJoinResponseDtoFromUser(savedUser);
	}

	private boolean checkDuplicatedEmail(String email) {
		return userRepository.existsByEmail(email);
	}
}
