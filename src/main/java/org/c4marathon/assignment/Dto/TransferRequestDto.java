package org.c4marathon.assignment.Dto;

import java.util.Optional;

import lombok.Getter;

@Getter
public class TransferRequestDto {
	private Long userId;
	private Long savingsAccountId;
	private Long externalUserId;
	private int money;
	private boolean externalTransfer;

	// 외부 사용자 간 송금 생성자
	public TransferRequestDto(Long userId, Optional<Long> externalUserId, int money) {
		this.userId = userId;
		this.externalUserId = externalUserId.orElse(null); // Optional 처리
		this.money = money;
		this.externalTransfer = true; // 외부 사용자 간 송금 여부
	}

	// 적금 계좌로 송금 생성자
	public TransferRequestDto(Long userId, Long savingsAccountId, int money) {
		this.userId = userId;
		this.savingsAccountId = savingsAccountId;
		this.money = money;
		this.externalTransfer = false; // 적금 계좌로 송금
	}
}
