package org.c4marathon.assignment;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.List;

import org.c4marathon.assignment.domain.Settlement;
import org.c4marathon.assignment.domain.SettlementType;
import org.c4marathon.assignment.service.SettlementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class SettlementControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Mock
	private SettlementService settlementService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	@DisplayName("정산 요청 API 테스트 - 성공")
	void testRequestSettlement() throws Exception {
		// Given
		int totalAmount = 30000;
		SettlementType type = SettlementType.EQUA;
		List<Long> participants = List.of(1L, 2L, 3L);

		Settlement mockSettlement = new Settlement(totalAmount, LocalDate.now(), type, participants,
			List.of(10000, 10000, 10000));
		when(settlementService.requestSettlement(anyInt(), any(SettlementType.class), anyList())).thenReturn(
			mockSettlement);

		// When & Then - API 호출 및 검증
		mockMvc.perform(post("/api/settlement/request")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"totalAmount\": 30000, \"type\": \"EQUA\", \"participants\": [1, 2, 3]}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.totalAmount").value(30000))
			.andExpect(jsonPath("$.settlementType").value("EQUA"))
			.andExpect(jsonPath("$.participants[0]").value(1))
			.andExpect(jsonPath("$.amounts[0]").value(10000));
	}

	@Test
	@DisplayName("정산 요청 조회 API 테스트 - 성공")
	void testGetSettlement() throws Exception {
		// Given
		Long settlementId = 1L;
		Settlement mockSettlement = new Settlement(20000, LocalDate.now(), SettlementType.EQUA, List.of(1L, 2L, 3L),
			List.of(6667, 6666, 6667));
		when(settlementService.getSettlement(settlementId)).thenReturn(mockSettlement);

		// When & Then - API 호출 및 검증
		mockMvc.perform(get("/api/settlement/{id}", settlementId)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.totalAmount").value(20000))
			.andExpect(jsonPath("$.settlementType").value("EQUA"))
			.andExpect(jsonPath("$.participants[0]").value(1))
			.andExpect(jsonPath("$.amounts[0]").value(6667))
			.andExpect(jsonPath("$.amounts[1]").value(6666))
			.andExpect(jsonPath("$.amounts[2]").value(6667));
	}

	@Test
	@DisplayName("정산 완료 API 테스트 - 성공")
	void testCompleteSettlement() throws Exception {
		// Given
		Long settlementId = 1L;
		Settlement mockSettlement = new Settlement(20000, LocalDate.now(), SettlementType.EQUA,
			List.of(1L, 2L, 3L), List.of(6667, 6666, 6667));

		// SettlementService에서 getSettlement가 호출되었을 때 mockSettlement 반환
		when(settlementService.getSettlement(settlementId)).thenReturn(mockSettlement);

		doNothing().when(settlementService).completeSettlement(settlementId);

		// When & Then - API 호출 및 응답 검증
		mockMvc.perform(put("/api/settlement/{id}/complete", settlementId)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isNoContent());  // 204 No Content 상태 코드 검증

		// verify: completeSettlement이 호출되었는지 확인
		verify(settlementService, times(1)).completeSettlement(settlementId);
	}
}
