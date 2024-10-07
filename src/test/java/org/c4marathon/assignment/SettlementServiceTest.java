package org.c4marathon.assignment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.c4marathon.assignment.domain.Settlement;
import org.c4marathon.assignment.domain.SettlementType;
import org.c4marathon.assignment.repository.SettlementRepository;
import org.c4marathon.assignment.service.SettlementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class SettlementServiceTest {

	@InjectMocks
	private SettlementService settlementService;

	@Mock
	private SettlementRepository settlementRepository;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	@DisplayName("1/n 정산이 정확히 계산되는지 테스트")
	void testDistributeEqually() {
		int totalAmount = 20000;
		int participantCount = 3;

		List<Integer> result = settlementService.distributeEqually(totalAmount, participantCount);

		// 각 참여자의 금액 확인
		assertEquals(6667, result.get(0));  // 첫 번째 참여자가 6667원을 받음
		assertEquals(6666, result.get(1));  // 두 번째 참여자가 6666원을 받음
		assertEquals(6667, result.get(2));  // 세 번째 참여자가 6667원을 받음

		// 금액의 총합이 정확한지 확인
		int sum = result.stream().mapToInt(Integer::intValue).sum();
		assertEquals(totalAmount, sum);
	}

	@Test
	@DisplayName("랜덤 정산이 정확히 계산되는지 테스트")
	void testDistributeRandomly() {
		int totalAmount = 20000;
		int participantCount = 3;

		List<Integer> result = settlementService.distributeRandomly(totalAmount, participantCount);

		// 금액의 총합이 정확한지 확인
		int sum = result.stream().mapToInt(Integer::intValue).sum();
		assertEquals(totalAmount, sum);
	}

	@Test
	@DisplayName("정산 요청 저장 테스트")
	void testRequestSettlement() {
		int totalAmount = 30000;
		SettlementType type = SettlementType.EQUA;
		List<Long> participants = List.of(1L, 2L, 3L);

		// Settlement 객체 생성 후 저장 로직 모킹
		Settlement mockSettlement = new Settlement(totalAmount, LocalDate.now(), type, participants,
			List.of(10000, 10000, 10000));
		when(settlementRepository.save(any(Settlement.class))).thenReturn(mockSettlement);

		Settlement result = settlementService.requestSettlement(totalAmount, type, participants);

		assertNotNull(result);
		assertEquals(totalAmount, result.getTotalAmount());
		assertEquals(type, result.getSettlementType()); // 수정된 메서드
		assertEquals(participants.size(), result.getParticipants().size());

		verify(settlementRepository, times(1)).save(any(Settlement.class));
	}

	@Test
	@DisplayName("정산 요청 조회 테스트")
	void testGetSettlement() {
		Long settlementId = 1L;
		Settlement mockSettlement = new Settlement(20000, LocalDate.now(), SettlementType.EQUA, List.of(1L, 2L, 3L),
			List.of(6667, 6666, 6667));
		when(settlementRepository.findById(settlementId)).thenReturn(Optional.of(mockSettlement));

		Settlement result = settlementService.getSettlement(settlementId);

		assertNotNull(result);
		assertEquals(mockSettlement.getTotalAmount(), result.getTotalAmount());
	}

	@Test
	@DisplayName("정산 완료 처리 테스트")
	void testCompleteSettlement() {
		Long settlementId = 1L;
		Settlement mockSettlement = mock(Settlement.class);
		when(settlementRepository.findById(settlementId)).thenReturn(Optional.of(mockSettlement));

		settlementService.completeSettlement(settlementId);

		verify(mockSettlement, times(1)).completeSettlement();
		verify(settlementRepository, times(1)).save(mockSettlement);
	}
}
