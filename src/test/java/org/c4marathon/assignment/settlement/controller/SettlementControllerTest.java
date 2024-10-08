package org.c4marathon.assignment.domain.settlement.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.c4marathon.assignment.domain.settlement.dto.request.SettlementRequestDto;
import org.c4marathon.assignment.domain.settlement.dto.response.SettlementHistoryResponseDto;
import org.c4marathon.assignment.domain.settlement.dto.response.SettlementResponseDto;
import org.c4marathon.assignment.domain.settlement.service.SettlementService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;

@WebMvcTest(SettlementController.class)
class SettlementControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private SettlementService settlementService;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	@DisplayName("정산 목록 조회")
	void findAllSettlementList() throws Exception {
		SettlementHistoryResponseDto settlementHistoryResponseDto1 = SettlementHistoryResponseDto.builder()
			.settlementId(1L)
			.build();
		SettlementHistoryResponseDto settlementHistoryResponseDto2 = SettlementHistoryResponseDto.builder()
			.settlementId(2L)
			.build();
		// given
		List<SettlementHistoryResponseDto> settlementHistoryList = List.of(
			settlementHistoryResponseDto1,
			settlementHistoryResponseDto2
		);
		given(settlementService.findAllSettlement(any(HttpServletRequest.class)))
			.willReturn(settlementHistoryList);

		// when, then
		mockMvc.perform(get("/settlement"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(2))
			.andExpect(jsonPath("$[0].settlementId").value(1L))
			.andExpect(jsonPath("$[0].name").value("정산 1"));
	}

	@Test
	@DisplayName("정산 요청")
	void requestSettlement() throws Exception {
		// given
		SettlementRequestDto requestDto = new SettlementRequestDto(1000L, "description", List.of("010-1234-5678"));
		SettlementResponseDto responseDto = new SettlementResponseDto(1L, "정산 요청 성공");

		given(settlementService.requestSettlement(any(SettlementRequestDto.class), any(HttpServletRequest.class)))
			.willReturn(responseDto);

		// when, then
		mockMvc.perform(post("/settlement/request")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestDto)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.settlementId").value(1L))
			.andExpect(jsonPath("$.message").value("정산 요청 성공"));
	}

	@Test
	@DisplayName("정산 수행")
	void performSettlement() throws Exception {
		// given
		RemittanceResponseDto responseDto = new RemittanceResponseDto("송금 성공");

		given(settlementService.settlementSplit(eq(1L), any(HttpServletRequest.class)))
			.willReturn(responseDto);

		// when, then
		mockMvc.perform(post("/settlement/1"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.responseMsg").value("송금 성공"));
	}
}
