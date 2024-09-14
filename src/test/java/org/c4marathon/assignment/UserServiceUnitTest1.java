package org.c4marathon.assignment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

import org.c4marathon.assignment.Dto.UserRequestDto;
import org.c4marathon.assignment.Dto.UserResponseDto;
import org.c4marathon.assignment.domain.Account;
import org.c4marathon.assignment.domain.User;
import org.c4marathon.assignment.repository.AccountRepository;
import org.c4marathon.assignment.repository.UserRepository;
import org.c4marathon.assignment.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class UserServiceUnitTest1 {

	@Mock
	private UserRepository userRepository;

	@Mock
	private AccountRepository accountRepository;

	@InjectMocks
	private UserService userService;

	@BeforeEach
	void setUp() {
		// Mockito 초기화
		MockitoAnnotations.openMocks(this);
	}

	@Test
	@DisplayName("회원가입 단위 테스트")
	public void registerUserTest() {
		// given
		UserRequestDto userRequestDto = UserRequestDto.builder()
			.password("lsk123")
			.name("이수경")
			.registrationNum("123456-7890123")
			.build();

		User mockUser = User.builder()
			.userId(1L)
			.password(userRequestDto.getPassword())
			.name(userRequestDto.getName())
			.registrationNum(userRequestDto.getRegistrationNum())
			.build();

		Account mockAccount = new Account("Main Account", 0, mockUser);
		mockUser.setMainAccount(mockAccount);

		// mock repository behaviors
		Mockito.when(userRepository.save(any(User.class)))
			.thenReturn(mockUser);

		// when
		UserResponseDto responseDto = userService.registerUser(userRequestDto);

		// then
		assertNotNull(responseDto);
		assertEquals(1L, responseDto.getUserId());
		assertEquals("이수경", responseDto.getName());
		assertEquals("123456-7890123", responseDto.getRegistrationNum());

		// 메인 계좌가 올바르게 설정되었는지 확인
		assertNotNull(mockUser.getMainAccount());
		assertEquals("Main Account", mockUser.getMainAccount().getAccountType());
		assertEquals(0, mockUser.getMainAccount().getBalance());

		// 성공 시 문구 출력
		System.out.println("회원가입 단위 테스트 성공!");
	}
}