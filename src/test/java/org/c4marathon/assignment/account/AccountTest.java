package org.c4marathon.assignment.account;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Optional;

import org.c4marathon.assignment.account.domain.Account;
import org.c4marathon.assignment.account.domain.AccountType;
import org.c4marathon.assignment.account.dto.ChargeDto;
import org.c4marathon.assignment.account.dto.SavingAccountPwDto;
import org.c4marathon.assignment.account.dto.SendDto;
import org.c4marathon.assignment.account.repository.AccountRepository;
import org.c4marathon.assignment.user.domain.User;
import org.c4marathon.assignment.user.dto.JoinDto;
import org.c4marathon.assignment.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
public class AccountTest {

	@MockBean
	private UserRepository userRepository;

	@MockBean
	private AccountRepository accountRepository;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	private String toJson(Object object) throws JsonProcessingException {
		return objectMapper.writeValueAsString(object);
	}

	@BeforeEach
	void start() {
		accountRepository.deleteAll();
	}

	@AfterEach
	void end() {
		accountRepository.deleteAll();
	}

	@DisplayName("[메인 계좌 충전 테스트]")
	@Test
	void chargeTest() throws Exception {
		// 회원가입 후 계좌 생성
		User user = new User("user123", "password", "홍길동", 1234);
		Account mainAccount = new Account(12345678L, AccountType.MAIN_ACCOUNT, 0, 1234, 3000000, user);

		// Mockito: accountRepository에서 특정 계좌번호로 조회될 때 반환값 설정
		given(accountRepository.findByAccount(12345678L)).willReturn(Optional.of(mainAccount));

		// 메인 계좌 가져오기
		Optional<Account> account = accountRepository.findByAccount(12345678L);

		// given: 충전할 계좌 번호와 금액
		ChargeDto chargeDto = new ChargeDto(account.get().getAccountNum(), 10000);

		// when, then: 해당 계좌에 충전 테스트 수행
		mockMvc.perform(post("/account/charge")
				.content(toJson(chargeDto))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk());
	}

	@DisplayName("[적금 계좌 생성 테스트]")
	@Test
	void savingTest() throws Exception {

		// 회원가입
		// given
		JoinDto joinDto = new JoinDto("aaaa", "ab12", "홍길동", 1234);

		// when, then
		mockMvc.perform(post("/user/join")
				.content(toJson(joinDto))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk());

		SavingAccountPwDto savingAccountPwDto = new SavingAccountPwDto(1000);

		// when, then
		mockMvc.perform(post("/account/create/{userId}", joinDto.userId())
				.content(toJson(savingAccountPwDto))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk());
	}

	@DisplayName("[송금 테스트]")
	@Test
	void sendTest() throws Exception {

		// given
		Long userId = 1L;
		SendDto loginDto = new SendDto(11111111L, 5000, 1234);

		// when, then
		mockMvc.perform(post("/account/send/{userId}")
				.content(toJson(userId))
				.content(toJson(loginDto))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk());
	}
}
