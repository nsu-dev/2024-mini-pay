package org.c4marathon.assignment.controller;

import org.c4marathon.assignment.Dto.TransferRequestDto;
import org.c4marathon.assignment.domain.AccountType;
import org.c4marathon.assignment.service.AccountService;
import org.c4marathon.assignment.service.QueueService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class AccountController {
	private final AccountService accountService;
	private final QueueService queueService;

	//적금 계좌 추가
	@PostMapping("/{userId}/savings")
	public ResponseEntity<Void> addSavingsAccount(@PathVariable Long userId,
		@RequestParam AccountType type,
		@RequestParam int balance) {
		accountService.addSavingsAccount(userId, type, balance);
		return ResponseEntity.ok().build();
	}

	//사용자 메인 계좌에서 적금 계좌로 송금
	@PostMapping("/{userId}/move-to-savings")
	public ResponseEntity<String> transferToSavings(@PathVariable Long userId,
		@RequestParam Long savingsAccountId,
		@RequestParam int money) {
		TransferRequestDto request = new TransferRequestDto(userId, savingsAccountId, money);
		queueService.addToQueue(request);
		return ResponseEntity.ok("송금 요청이 접수되었습니다.");
	}

	// 외부 메인 계좌로 송금
	@PostMapping("/{userId}/transfer-to-external/{externalUserId}")
	public ResponseEntity<String> transferToExternalMainAccount(@PathVariable Long userId,
		@PathVariable Long externalUserId,
		@RequestParam int money) {
		TransferRequestDto request = new TransferRequestDto(userId, externalUserId, money);
		queueService.addToQueue(request);
		return ResponseEntity.ok("송금 요청이 접수되었습니다.");
	}
}
