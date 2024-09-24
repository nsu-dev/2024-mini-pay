package org.c4marathon.assignment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.c4marathon.assignment.Dto.UserRequestDto;
import org.c4marathon.assignment.Dto.UserResponseDto;
import org.c4marathon.assignment.domain.Account;
import org.c4marathon.assignment.domain.AccountType;
import org.c4marathon.assignment.domain.User;
import org.c4marathon.assignment.repository.AccountRepository;
import org.c4marathon.assignment.repository.UserRepository;
import org.c4marathon.assignment.service.AccountService;
import org.c4marathon.assignment.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private AccountRepository accountRepository;

	@InjectMocks
	private UserService userService;

	@InjectMocks
	private AccountService accountService;

	private User user;
	private User externalUser;
	private Account userMainAccount;
	private Account externalMainAccount;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		user = new User(1L, "lsk123", "이수경", "123456-7890123");
		externalUser = new User(2L, "kim123", "김철수", "987654-3210987");

		userMainAccount = new Account(AccountType.MAIN, 1000000, user);
		externalMainAccount = new Account(AccountType.MAIN, 2000000, externalUser);

		user.setMainAccount(userMainAccount);
		externalUser.setMainAccount(externalMainAccount);

		when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));
		when(userRepository.findById(externalUser.getUserId())).thenReturn(Optional.of(externalUser));
	}

	@Test
	@DisplayName("회원가입 단위 테스트")
	public void registerUserTest() {
		// given
		UserRequestDto userRequestDto = new UserRequestDto(
			null,
			"lsk123",
			"이수경",
			"123456-789123"
		);

		// User 객체를 생성자 방식으로 생성
		User mockUser = new User(1L, "lsk123", "이수경", "123456-7890123");
		Account mockAccount = new Account(AccountType.MAIN, 0, mockUser);
		mockUser.setMainAccount(mockAccount);

		// mock repository behaviors
		when(userRepository.save(any(User.class))).thenReturn(mockUser);

		// when
		UserResponseDto responseDto = userService.registerUser(userRequestDto);

		// then
		assertNotNull(responseDto);
		assertEquals(1L, responseDto.getUserId());
		assertEquals("이수경", responseDto.getName());
		assertEquals("123456-789123", responseDto.getRegistrationNum());

		// 메인 계좌가 올바르게 설정되었는지 확인
		assertNotNull(mockUser.getMainAccount());
		assertEquals(AccountType.MAIN, mockUser.getMainAccount().getType());
		assertEquals(0, mockUser.getMainAccount().getBalance());

		// 성공 시 문구 출력
		System.out.println("회원가입 단위 테스트 성공!");
	}

	@Test
	@DisplayName("적금 계좌 추가 테스트")
	public void addSavingsAccountTest() {
		// given
		Long userId = 1L;
		AccountType type = AccountType.SAVINGS;
		int initialBalance = 100000;

		// User 객체를 Mock으로 설정
		User mockUser = new User(
			1L,    // 테스트용 userId 설정
			"lsk123",
			"이수경",
			"123456-7890123"
		);

		// 저장된 User를 반환하도록 설정
		Mockito.when(userRepository.findById(userId))
			.thenReturn(java.util.Optional.of(mockUser));

		// ArgumentCaptor를 사용해 저장되는 Account 객체를 캡처
		ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);

		// when
		accountService.addSavingsAccount(userId, type, initialBalance);

		// then
		// 적금 계좌가 AccountRepository에 저장되는지 확인
		verify(accountRepository, times(1)).save(accountCaptor.capture());

		Account savedAccount = accountCaptor.getValue();
		assertNotNull(savedAccount);
		assertEquals(type, savedAccount.getType());
		assertEquals(initialBalance, savedAccount.getBalance());
		assertEquals(mockUser, savedAccount.getUser()); // User가 null이 아닌지 확인

		// 계좌가 올바르게 추가되었는지 확인 (User 객체에 계좌가 추가되었는지 체크)
		assertEquals(1, mockUser.getSavingAccounts().size());
		Account addedAccount = mockUser.getSavingAccounts().get(0);
		assertEquals(type, addedAccount.getType());
		assertEquals(initialBalance, addedAccount.getBalance());

		// 성공 메시지 출력
		System.out.println("적금 계좌 추가 테스트 성공!");
	}

	@Test
	@DisplayName("메인 계좌에서 잔액 부족으로 송금 실패 테스트")
	public void transferToSavingsFailTest() {
		// given
		Long userId = 1L;
		Long savingsAccountId = 2L;
		int transferAmount = 150000; // 메인 계좌 잔액보다 많은 금액

		// 사용자와 계좌 객체 Mock 설정
		User mockUser = new User(
			1L, "lsk123", "이수경", "123456-7890123"
		);

		Account mainAccount = new Account(AccountType.MAIN, 100000, mockUser); // 메인 계좌에 100,000원
		Account savingsAccount = new Account(AccountType.SAVINGS, 0, mockUser); // 적금 계좌는 0원

		mockUser.setMainAccount(mainAccount);

		// 저장된 사용자와 계좌 반환 설정
		when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

		// lenient()로 불필요한 stubbing 경고 무시
		lenient().when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
		lenient().when(accountRepository.findById(savingsAccountId)).thenReturn(Optional.of(savingsAccount));

		// when & then (잔액 부족으로 예외 발생 확인)
		assertThrows(IllegalArgumentException.class, () -> {
			accountService.transferToSavings(userId, savingsAccountId, transferAmount);
		});

		// 성공 메시지 출력
		System.out.println("메인 계좌에서 잔액 부족으로 송금 실패 테스트 성공!");
	}

	@Test
	@DisplayName("외부 유저의 메인 계좌에서 사용자 메인 계좌로 돈 이동 성공 테스트")
	public void transferSuccessTest() {
		// given
		Long userId = 1L;
		Long externalUserId = 2L;
		int transferMoney = 500000;

		// when
		boolean success = accountService.transferFromExternalAccount(userId, externalUserId, transferMoney);

		// then
		assertTrue(success);
		assertEquals(1000000, userMainAccount.getBalance()); // 사용자의 메인 계좌 잔액
		assertEquals(2000000, externalMainAccount.getBalance()); // 외부 유저의 메인 계좌 잔액

		// 성공 시 문구 출력
		System.out.println("돈 이동 성공 테스트 성공!");
	}

	@Test
	@DisplayName("외부 유저의 잔액 부족으로 인한 돈 이동 실패 테스트")
	public void transferInsufficientFundsTest() {
		// given
		Long userId = 1L;
		Long externalUserId = 2L;
		int transferAmount = 500000;

		// 외부 유저의 잔액 부족 설정
		externalMainAccount = new Account(AccountType.MAIN, 300000, externalUser);
		externalUser.setMainAccount(externalMainAccount);

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(userRepository.findById(externalUserId)).thenReturn(Optional.of(externalUser));

		// when & then (잔액 부족으로 예외 발생 확인)
		assertThrows(IllegalArgumentException.class, () -> {
			accountService.transferFromExternalAccount(userId, externalUserId, transferAmount);
		});

		// 성공 메시지 출력
		System.out.println("잔액 부족으로 인한 돈 이동 실패 테스트 성공!");
	}
}