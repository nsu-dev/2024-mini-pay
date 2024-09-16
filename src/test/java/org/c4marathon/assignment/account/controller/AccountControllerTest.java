package org.c4marathon.assignment.account.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.c4marathon.assignment.domain.account.controller.AccountController;
import org.c4marathon.assignment.domain.account.dto.CreateResponseDto;
import org.c4marathon.assignment.domain.account.dto.RemittanceRequestDto;
import org.c4marathon.assignment.domain.account.dto.RemittanceResponseDto;
import org.c4marathon.assignment.domain.account.dto.SavingRequestDto;
import org.c4marathon.assignment.domain.account.service.AccountService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(AccountController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AccountControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private AccountService accountService;

	@DisplayName("메인 계좌 충전 테스트")
	@Test
	public void testChargeMain() throws Exception {
		RemittanceRequestDto requestDto = RemittanceRequestDto.builder().build();
		RemittanceResponseDto responseDto = RemittanceResponseDto.builder().build();
		when(accountService.chargeMain(any(RemittanceRequestDto.class), anyLong(), any())).thenReturn(responseDto);

		mockMvc.perform(post("/account/remittance/1")
			.contentType(MediaType.APPLICATION_JSON)
			.content(new ObjectMapper().writeValueAsString(requestDto))
		).andExpect(status().isOk());
	}
	@DisplayName("적금계좌 생성 api 테스트")
	@Test
	public void testCreateAccount() throws Exception {
		CreateResponseDto responseDto = CreateResponseDto.builder().build();
		when(accountService.createAccountOther(anyLong(), anyString(), any())).thenReturn(responseDto);

		mockMvc.perform(post("/account/creataccount/role/1")
			.contentType(MediaType.APPLICATION_JSON)
			.content(new ObjectMapper().writeValueAsString(responseDto))
		).andExpect(status().isOk());
	}

	@Test
	@DisplayName("적금계좌 입급 api 테스트")
	public void testSavingRemittance() throws Exception {
		SavingRequestDto requestDto = SavingRequestDto.builder().build();
		RemittanceResponseDto responseDto = RemittanceResponseDto.builder().build();
		when(accountService.savingRemittance(anyLong(), any(SavingRequestDto.class), any())).thenReturn(responseDto);

		mockMvc.perform(post("/account/saving/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(new ObjectMapper().writeValueAsString(requestDto))
		).andExpect(status().isOk());
	}
}
