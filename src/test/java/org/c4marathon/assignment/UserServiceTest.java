package org.c4marathon.assignment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.c4marathon.assignment.Dto.UserRequestDto;
import org.c4marathon.assignment.Dto.UserResponseDto;
import org.c4marathon.assignment.domain.User;
import org.c4marathon.assignment.repository.UserRepository;
import org.c4marathon.assignment.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private UserService userService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	@DisplayName("사용자 회원가입 테스트")
	public void testRegisterUser() {
		// Given
		UserRequestDto requestDto = new UserRequestDto(1L, "testPass", "testUser", "123456-7891234");
		User user = User.builder()
			.name("testUser")
			.password("testPass")
			.registrationNum("123456-7891234")
			.build();

		// Mocking repository save
		when(userRepository.save(any(User.class))).thenReturn(user);

		// When
		UserResponseDto response = userService.registerUser(requestDto);

		// Then
		assertNotNull(response);
		assertEquals("testUser", response.getName());
		verify(userRepository, times(1)).save(any(User.class));
	}
}