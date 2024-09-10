package org.c4marathon.assignment.user.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.c4marathon.assignment.domain.user.dto.LoginRequestDto;
import org.c4marathon.assignment.domain.user.dto.LoginResponseDto;
import org.c4marathon.assignment.domain.user.dto.UserDto;
import org.c4marathon.assignment.domain.user.dto.UserMapper;
import org.c4marathon.assignment.domain.user.entity.User;
import org.c4marathon.assignment.domain.user.repository.UserRepository;
import org.c4marathon.assignment.domain.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private UserService userService;

	private User user;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}
	// @DisplayName("요청 받은 유저 정보를 저장하고, 응답 메시지를 검증한다.")
	// @Test
	// void join() {
	// 	//given
	// 	UserDto userDto = new UserDto("010-8337-6023", "조아빈", "20000604", "pw123");
	// 	Mockito.when(userRepository.save(any(User.class))).thenReturn(UserMapper.toUser(userDto));
	//
	// 	//when
	// 	JoinResponseDto joinResponseDto = userService.join(userDto);
	//
	// 	//then
	// 	ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
	// 	verify(userRepository).save(userCaptor.capture());
	// 	User capturedUser = userCaptor.getValue();
	//
	// 	assertThat(capturedUser.getUserPhone()).isEqualTo(userDto.getUserPhone());
	// 	assertThat(capturedUser.getUserPassword()).isEqualTo(userDto.getPassword());
	// 	assertThat(capturedUser.getUserName()).isEqualTo(userDto.getUserName());
	// 	assertThat(capturedUser.getUserBirth()).isEqualTo(userDto.getUserBirth());
	//
	// 	assertThat(joinResponseDto.responseMsg()).isEqualTo("회원가입 성공!");
	// }

	@DisplayName("등록된 유저의 로그인이 정상적으로 진행되는지 검사한다.")
	@Test
	void login() {
		//given
		LoginRequestDto loginRequestDto = new LoginRequestDto("010-8337-6023", "pw123");

		User mockUser = UserMapper.toUser(new UserDto("010-8337-6023", "조아빈", "20000604", "pw123"));

		//when
		when(userRepository.existsByUserPhone("010-8337-6023")).thenReturn(true);
		when(userRepository.findByUserPhone("010-8337-6023")).thenReturn(mockUser);

		//then
		LoginResponseDto loginResponseDto = userService.login(loginRequestDto);
		assertThat(loginResponseDto.responseMsg()).isEqualTo("로그인 성공!");
	}
}
