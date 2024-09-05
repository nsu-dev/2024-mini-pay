package org.c4marathon.assignment.user.service;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import org.c4marathon.assignment.user.domain.User;
import org.c4marathon.assignment.user.dto.request.JoinRequestDto;
import org.c4marathon.assignment.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private UserRepository userRepository;

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
}
