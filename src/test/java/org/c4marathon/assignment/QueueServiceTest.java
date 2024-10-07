package org.c4marathon.assignment;

import static org.mockito.Mockito.*;

import org.c4marathon.assignment.Dto.TransferRequestDto;
import org.c4marathon.assignment.service.AccountService;
import org.c4marathon.assignment.service.QueueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class QueueServiceTest {

	@Mock
	private AccountService accountService;

	@InjectMocks
	private QueueService queueService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);  // Mockito 초기화
	}

	@Test
	@DisplayName("송금 요청이 큐에 추가되면 처리되는지 테스트")
	public void testAddToQueue() throws InterruptedException {
		// Given
		TransferRequestDto request = new TransferRequestDto(1L, 2L, 3L, 10000, false);

		// When
		queueService.addToQueue(request);

		// 큐에서 송금 처리가 되었다고 가정
		verify(accountService, timeout(1000).times(1))
			.transferToSavings(1L, 3L, 10000);
	}

	@Test
	@DisplayName("외부 송금 요청 처리 테스트")
	public void testExternalTransferRequest() throws InterruptedException {
		// Given
		TransferRequestDto request = new TransferRequestDto(1L, 2L, 3L, 50000, true);

		// When
		queueService.addToQueue(request);

		// Then
		verify(accountService, timeout(1000).times(1))
			.transferFromExternalAccount(1L, 2L, 50000);
	}
}