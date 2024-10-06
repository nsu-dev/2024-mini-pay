package org.c4marathon.assignment;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.c4marathon.assignment.Dto.TransferRequestDto;
import org.c4marathon.assignment.Dto.UserRequestDto;
import org.c4marathon.assignment.Dto.UserResponseDto;
import org.c4marathon.assignment.Exception.InsufficientBalanceException;
import org.c4marathon.assignment.domain.Account;
import org.c4marathon.assignment.repository.AccountRepository;
import org.c4marathon.assignment.service.AccountService;
import org.c4marathon.assignment.service.QueueService;
import org.c4marathon.assignment.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class UserTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private UserService userService;

	@MockBean
	private AccountService accountService;

	@MockBean
	private QueueService queueService;

	@Mock
	private AccountRepository accountRepository;

	@Autowired
	private ObjectMapper objectMapper;

	private static final Long USER_ID = 1L;
	private static final Long EXTERNAL_USER_ID = 2L;
	private static final Long SAVINGS_ACCOUNT_ID = 3L;
	private static final int MONEY = 500000;

	@Test
	@DisplayName("회원가입 API 테스트")
	public void registerUserTest() throws Exception {
		// given - 회원가입 요청 DTO
		UserRequestDto requestDto = new UserRequestDto(
			null,
			"lsk123",
			"이수경",
			"123456-789123"
		);

		// Mock 설정
		when(userService.registerUser(any(UserRequestDto.class)))
			.thenReturn(new UserResponseDto(1L, "이수경", "123456-789123"));

		// when - MockMvc를 통해 API 호출
		mockMvc.perform(post("/users/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestDto)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.userId").value(1L))
			.andExpect(jsonPath("$.name").value("이수경"))
			.andExpect(jsonPath("$.registrationNum").value("123456-789123"));
	}

	@Test
	@DisplayName("중복 사용자 등록 테스트")
	public void registerUser_Duplicate_Test() throws Exception {
		// Given - 중복 사용자 등록 시 예외 던지기 설정
		UserRequestDto requestDto = new UserRequestDto(null, "duplicateuser", "password123", "123456-789123");
		when(userService.registerUser(any(UserRequestDto.class)))
			.thenThrow(new IllegalArgumentException("이미 존재하는 사용자입니다."));

		// When - API 호출
		mockMvc.perform(post("/users/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestDto)))
			// Then - 중복 사용자 예외 확인
			.andExpect(status().isBadRequest())
			.andExpect(content().string("잘못된 요청: 이미 존재하는 사용자입니다."));

		// Verify - 서비스 호출 여부 확인
		verify(userService, times(1)).registerUser(any(UserRequestDto.class));
	}

	@Test
	@DisplayName("외부 유저 메인 계좌에서 사용자 메인 계좌로 송금 요청 테스트")
	public void transferFromExternalAccountTest() throws Exception {
		// Given
		TransferRequestDto request = new TransferRequestDto(USER_ID, EXTERNAL_USER_ID, MONEY);

		// 큐 서비스의 addToQueue 메서드가 호출될 때 request가 전달되는지 확인
		doNothing().when(queueService).addToQueue(any(TransferRequestDto.class));

		// When - API 호출
		mockMvc.perform(post("/users/{userId}/transfer-from-external/{externalUserId}", USER_ID, EXTERNAL_USER_ID)
				.param("money", String.valueOf(MONEY))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(content().string("이체 요청이 접수되었습니다."));

		// Then - 큐 서비스의 addToQueue 메서드가 호출되었는지 검증
		verify(queueService, times(1)).addToQueue(any(TransferRequestDto.class));
	}

	// @Test
	// @DisplayName("사용자 메인 계좌에서 적금 계좌로 송금 요청 테스트")
	// public void transferToSavingsAccountTest() throws Exception {
	// 	// Given
	// 	TransferRequestDto request = new TransferRequestDto(USER_ID, SAVINGS_ACCOUNT_ID, MONEY);
	//
	// 	// 큐 서비스의 addToQueue 메서드가 호출될 때 request가 전달되는지 확인
	// 	doNothing().when(queueService).addToQueue(any(TransferRequestDto.class));
	//
	// 	// When - API 호출
	// 	mockMvc.perform(post("/users/{userId}/move-to-savings", USER_ID)
	// 			.param("savingsAccountId", String.valueOf(SAVINGS_ACCOUNT_ID))
	// 			.param("money", String.valueOf(MONEY))
	// 			.contentType(MediaType.APPLICATION_JSON))
	// 		.andExpect(status().isOk())
	// 		.andExpect(content().string("송금 요청이 접수되었습니다."));
	//
	// 	// Then - 큐 서비스의 addToQueue 메서드가 호출되었는지 검증
	// 	verify(queueService, times(1)).addToQueue(any(TransferRequestDto.class));
	// }

	@Test
	@DisplayName("메인 계좌에서 적금 계좌로 송금할 때, 잔액 부족으로 인한 예외 발생 테스트")
	public void transferToSavingsAccount_InsufficientBalance_Test() throws Exception {
		// Given
		int insufficientAmount = 600_000; // 메인 계좌 잔액보다 많은 금액을 출금 시도
		TransferRequestDto request = new TransferRequestDto(USER_ID, SAVINGS_ACCOUNT_ID, insufficientAmount);

		// Mock 설정 - 잔액이 부족한 상황에서 transferToSavings 메서드 호출 시 예외를 던지도록 설정
		doThrow(new InsufficientBalanceException("잔액이 부족합니다.")).when(accountService)
			.transferToSavings(USER_ID, SAVINGS_ACCOUNT_ID, insufficientAmount);

		// When - API 호출 시 예외 발생
		mockMvc.perform(post("/users/{userId}/move-to-savings", USER_ID)
				.param("savingsAccountId", String.valueOf(SAVINGS_ACCOUNT_ID))
				.param("money", String.valueOf(insufficientAmount))
				.contentType(MediaType.APPLICATION_JSON))
			// Then - 잔액 부족 예외가 발생하고 적절한 메시지 반환
			.andExpect(status().isBadRequest())
			.andExpect(content().string("잘못된 요청: 잔액이 부족합니다."));

		// Then - 실제로 송금이 이루어지지 않았는지 확인
		verify(accountService, times(1)).transferToSavings(USER_ID, SAVINGS_ACCOUNT_ID, insufficientAmount);
		verify(accountRepository, never()).save(any(Account.class)); // 적금 계좌에 돈이 저장되지 않음을 검증
	}
}