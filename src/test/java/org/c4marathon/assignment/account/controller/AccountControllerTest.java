package org.c4marathon.assignment.account.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.c4marathon.assignment.domain.account.controller.AccountController;
import org.c4marathon.assignment.domain.account.dto.CreateResponseDto;
import org.c4marathon.assignment.domain.account.dto.RemittanceRequestDto;
import org.c4marathon.assignment.domain.account.dto.RemittanceResponseDto;
import org.c4marathon.assignment.domain.account.dto.SavingRequestDto;
import org.c4marathon.assignment.domain.account.entity.CreateResponseMsg;
import org.c4marathon.assignment.domain.account.entity.RemittanceResponseMsg;
import org.c4marathon.assignment.domain.account.service.AccountService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

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
		RemittanceResponseDto remittanceResponseDto = new RemittanceResponseDto(RemittanceResponseMsg.SUCCESS.getResponseMsg());

		// 서비스 계층이 호출될 때 반환할 값을 미리 설정
		given(accountService.chargeMain(any(RemittanceRequestDto.class))).willReturn(remittanceResponseDto);

		// when & then
		mockMvc.perform(post("/account/remittance")  // POST 요청
				.contentType(MediaType.APPLICATION_JSON)  // 요청 본문의 타입 지정 (JSON)
				.content(objectMapper.writeValueAsString(remittanceRequestDto))  // 요청 본문에 remittanceRequestDto를 JSON으로 변환하여 전달
				.with(csrf())
				.characterEncoding("utf-8"))
			.andExpect(status().isOk())  // 응답 상태 코드가 200인지 확인
			.andExpect(jsonPath("$.responseMsg").value(RemittanceResponseMsg.SUCCESS.getResponseMsg()));  // 응답 본문에 있는 responseMsg 확인
	}
	@DisplayName("적금계좌 생성 api 테스트")
	@Test
	public void testCreateAccount() throws Exception {
		CreateResponseDto responseDto = new CreateResponseDto(CreateResponseMsg.SUCCESS.getResponseMsg());
		when(accountService.createAccountOther(anyString(), any())).thenReturn(responseDto);

		mockMvc.perform(post("/account/creataccount/role")
			.contentType(MediaType.APPLICATION_JSON)
			.content(new ObjectMapper().writeValueAsString(responseDto))
		).andExpect(status().isOk());
	}

	@Test
	@DisplayName("적금계좌 입급 api 테스트")
	public void testSavingRemittance() throws Exception {
		SavingRequestDto requestDto = new SavingRequestDto(30000);
		RemittanceResponseDto remittanceResponseDto = new RemittanceResponseDto(RemittanceResponseMsg.SUCCESS.getResponseMsg());
		when(accountService.savingRemittance(anyLong(), any(SavingRequestDto.class), any())).thenReturn(remittanceResponseDto);

		mockMvc.perform(post("/account/saving/1")
			.contentType(MediaType.APPLICATION_JSON)
			.content(new ObjectMapper().writeValueAsString(requestDto))
		).andExpect(status().isOk());
	}

	@Test
	@DisplayName("메인계좌간 거래 api")
	public void testRemittanceMain() throws Exception{
		RemittanceRequestDto remittanceRequestDto = new RemittanceRequestDto(3288494829384L, 100000L);
		RemittanceResponseDto remittanceResponseDto = new RemittanceResponseDto(RemittanceResponseMsg.SUCCESS.getResponseMsg());

		when(accountService.remittanceOtherMain(any(RemittanceRequestDto.class), any())).thenReturn(remittanceResponseDto);
		mockMvc.perform(post("/account/remittance/other")
			.contentType(MediaType.APPLICATION_JSON)
			.content(new ObjectMapper().writeValueAsString(remittanceRequestDto))
		).andExpect(status().isOk());

	}
}

