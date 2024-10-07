package org.c4marathon.assignment.account.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.c4marathon.assignment.domain.account.dto.request.RemittanceRequestDto;
import org.c4marathon.assignment.domain.account.dto.request.SavingRequestDto;
import org.c4marathon.assignment.domain.account.dto.response.CreateResponseDto;
import org.c4marathon.assignment.domain.account.dto.response.RemittanceResponseDto;
import org.c4marathon.assignment.domain.account.entity.responsemsg.AccountErrCode;
import org.c4marathon.assignment.domain.account.entity.responsemsg.CreateResponseMsg;
import org.c4marathon.assignment.domain.account.entity.responsemsg.RemittanceResponseMsg;
import org.c4marathon.assignment.domain.account.exception.AccountException;
import org.c4marathon.assignment.domain.account.service.AccountService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "testUser", roles = {"USER"})
public class AccountControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private AccountService accountService;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	@DisplayName("메인 계좌 충전 API 테스트")
	void chargeMain() throws Exception {
		// given
		RemittanceRequestDto remittanceRequestDto = new RemittanceRequestDto(3288494829384L, 100000L);
		RemittanceResponseDto remittanceResponseDto = new RemittanceResponseDto(
			RemittanceResponseMsg.SUCCESS.getResponseMsg());

		// 서비스 계층이 호출될 때 반환할 값을 미리 설정
		given(accountService.chargeMain(any(RemittanceRequestDto.class))).willReturn(remittanceResponseDto);

		// when & then
		mockMvc.perform(post("/account/remittance")  // POST 요청
				.contentType(MediaType.APPLICATION_JSON)  // 요청 본문의 타입 지정 (JSON)
				.content(
					objectMapper.writeValueAsString(remittanceRequestDto))  // 요청 본문에 remittanceRequestDto를 JSON으로 변환하여 전달
				.with(csrf())
				.characterEncoding("utf-8"))
			.andExpect(status().isOk())  // 응답 상태 코드가 200인지 확인
			.andExpect(jsonPath("$.responseMsg").value(
				RemittanceResponseMsg.SUCCESS.getResponseMsg()));  // 응답 본문에 있는 responseMsg 확인
	}

	@DisplayName("적금계좌 생성 api 테스트")
	@Test
	void testCreateAccount() throws Exception {
		CreateResponseDto responseDto = new CreateResponseDto(CreateResponseMsg.SUCCESS.getResponseMsg());
		when(accountService.createAccountOther(anyString(), any())).thenReturn(responseDto);

		mockMvc.perform(post("/account/creataccount/role")
			.contentType(MediaType.APPLICATION_JSON)
			.content(new ObjectMapper().writeValueAsString(responseDto))
		).andExpect(status().isOk());
	}

	@Test
	@DisplayName("적금계좌 입급 api 테스트")
	void testSavingRemittance() throws Exception {
		SavingRequestDto requestDto = new SavingRequestDto(30000);
		RemittanceResponseDto remittanceResponseDto = new RemittanceResponseDto(
			RemittanceResponseMsg.SUCCESS.getResponseMsg());
		when(accountService.savingRemittance(anyLong(), any(SavingRequestDto.class), any())).thenReturn(
			remittanceResponseDto);

		mockMvc.perform(post("/account/saving/1")
			.contentType(MediaType.APPLICATION_JSON)
			.content(new ObjectMapper().writeValueAsString(requestDto))
		).andExpect(status().isOk());
	}

	@Test
	@DisplayName("메인계좌간 거래 api")
	void testRemittanceMain() throws Exception {
		RemittanceRequestDto remittanceRequestDto = new RemittanceRequestDto(3288494829384L, 100000L);
		RemittanceResponseDto remittanceResponseDto = new RemittanceResponseDto(
			RemittanceResponseMsg.SUCCESS.getResponseMsg());

		when(accountService.remittanceOtherMain(any(RemittanceRequestDto.class), any())).thenReturn(
			remittanceResponseDto);
		mockMvc.perform(post("/account/remittance/other")
			.contentType(MediaType.APPLICATION_JSON)
			.content(new ObjectMapper().writeValueAsString(remittanceRequestDto))
		).andExpect(status().isOk());
	}

	@Test
	@DisplayName("컨트롤러 단에서 런타임예외발생 시 잡아서 잘 처리하는지 검증")
	void accountRuntimeExceptionFromController() throws Exception {
		RemittanceRequestDto remittanceRequestDto = new RemittanceRequestDto(3288494829384L, 100000L);
		given(accountService.chargeMain(any(RemittanceRequestDto.class))).willThrow(new RuntimeException());

		mockMvc.perform(post("/account/remittance")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(remittanceRequestDto))
				.with(csrf())
				.characterEncoding("utf-8"))
			.andExpect(status().isInternalServerError());
	}

	@Test
	@DisplayName("컨트롤러 단에서 AccountException 발생 시 잡아서 잘 처리하는지 검증")
	void accountExceptionFromController() throws Exception {
		RemittanceRequestDto remittanceRequestDto = new RemittanceRequestDto(3288494829384L, 100000L);
		given(accountService.chargeMain(any(RemittanceRequestDto.class))).willThrow(
			new AccountException(AccountErrCode.ACCOUNT_UNAVAILABLE));

		mockMvc.perform(post("/account/remittance")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(remittanceRequestDto))
				.with(csrf())
				.characterEncoding("utf-8"))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("컨트롤러 단에서 validation 에러 발생 시 잡아서 잘 처리하는지 검증")
	void accountValidationFromController() throws Exception {
		RemittanceRequestDto remittanceRequestDto = new RemittanceRequestDto(null, 100000L);
		mockMvc.perform(post("/account/remittance")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(remittanceRequestDto))
				.with(csrf())
				.characterEncoding("utf-8"))
			.andExpect(status().isBadRequest());
	}
}

