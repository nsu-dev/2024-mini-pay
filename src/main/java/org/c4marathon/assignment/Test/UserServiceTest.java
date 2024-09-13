package org.c4marathon.assignment.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.c4marathon.assignment.Dto.UserRequestDto;
import org.c4marathon.assignment.Dto.UserResponseDto;
import org.c4marathon.assignment.domain.Account;
import org.c4marathon.assignment.domain.User;
import org.c4marathon.assignment.repository.AccountRepository;
import org.c4marathon.assignment.repository.UserRepository;
import org.c4marathon.assignment.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class UserServiceTest {

	@InjectMocks
	private UserService userService;

	@Mock
	private UserRepository userRepository;

	@Mock
	private AccountRepository accountRepository;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	//회원가입 테스트
	@Test
	void registerUser_success() {
		// Given: 테스트에 필요한 DTO와 엔티티 객체를 생성
		UserRequestDto userRequestDto = UserRequestDto.builder()
			.password("lsk123")
			.name("이수경")
			.registrationNum(123456789)
			.build();

		User user = User.builder()
			.password("lsk123")
			.name("이수경")
			.registrationNum(123456789)
			.build();

		// Mock: userRepository가 user 객체를 저장하는 동작을 흉내냄
		when(userRepository.save(any(User.class))).thenReturn(user);

		// When: registerUser 호출
		UserResponseDto responseDto = userService.registerUser(userRequestDto);

		// Then: 결과 검증
		assertNotNull(responseDto);
		assertEquals("이수경", responseDto.getName());
		assertEquals(123456789, responseDto.getRegistrationNum());
	}

	//적금 계좌 추가 테스트
	@Test
	void addSavingsAccount_success() {
		// Given: 테스트에 필요한 User 객체를 생성
		User user = User.builder()
			.password("lsk123")
			.name("이수경")
			.registrationNum(123456789)
			.build();

		// Mock: userRepository가 사용자 조회 시 User 객체를 반환하도록 설정
		when(userRepository.findById(anyLong())).thenReturn(java.util.Optional.of(user));

		// When: addSavingsAccount 메서드 호출
		userService.addSavingsAccount(1L, "Savings Account", 10000);

		// Then: accountRepository의 save 메서드가 정확히 1번 호출되었는지 확인
		verify(accountRepository, times(1)).save(any());
	}

	//메인 계좌에서 적금계좌로 송금
	@Test
	void transferToSavings_success() {
		// Given: 테스트에 필요한 User 및 Account 객체 생성
		User user = User.builder()
			.password("lsk123")
			.name("이수경")
			.registrationNum(123456789)
			.build();
		Account mainAccount = new Account("Main Account", 5000, user);
		Account savingsAccount = new Account("Savings Account", 0, user);

		user.setMainAccount(mainAccount);
		user.addSavingAccount("Savings Account", 0);

		// Mock: userRepository와 accountRepository의 동작 설정
		when(userRepository.findById(anyLong())).thenReturn(java.util.Optional.of(user));
		when(accountRepository.findById(anyLong())).thenReturn(java.util.Optional.of(savingsAccount));

		// When: 메인 계좌에서 적금 계좌로 송금
		boolean success = userService.transferToSavings(1L, 1L, 3000);

		// Then: 송금 성공 여부 및 계좌 잔액 확인
		assertTrue(success);
		assertEquals(2000, mainAccount.getBalance());
		assertEquals(3000, savingsAccount.getBalance());
	}
}
