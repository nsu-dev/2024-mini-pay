package org.c4marathon.assignment.service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.c4marathon.assignment.Dto.TransferRequestDto;
import org.springframework.stereotype.Service;

@Service
public class QueueService {
	// 송금 요청을 관리하는 큐
	private final BlockingQueue<TransferRequestDto> queue = new LinkedBlockingQueue<>();
	private final AccountService accountService;

	public QueueService(AccountService accountService) {
		this.accountService = accountService;

		// 큐에서 송금 요청을 비동기적으로 처리하는 스레드 실행
		new Thread(this::processQueue).start();
	}

	// 큐에 송금 요청 추가
	public void addToQueue(TransferRequestDto request) {
		queue.add(request);
	}

	// 큐에서 송금 요청을 순차적으로 처리
	private void processQueue() {
		while (true) {
			try {
				TransferRequestDto request = queue.take(); // 큐에서 요청 가져오기
				if (request.isExternalTransfer()) {
					// 외부 메인 계좌로 송금
					accountService.transferFromExternalAccount(request.getUserId(), request.getExternalUserId(),
						request.getMoney());
				} else {
					// 적금 계좌로 송금
					accountService.transferToSavings(request.getUserId(), request.getSavingsAccountId(),
						request.getMoney());
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			} catch (Exception e) {
				System.out.println("송금 처리 중 오류 발생: " + e.getMessage());
			}
		}
	}

}
