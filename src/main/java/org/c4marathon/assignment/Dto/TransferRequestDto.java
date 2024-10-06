package org.c4marathon.assignment.Dto;

import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequestDto {
	private Long userId;
	private Long externalUserId;
	private Long savingsAccountId;
	private int money;
	private boolean externalTransfer;

	// 외부 사용자 간 송금 생성자
	public TransferRequestDto(Long userId, Optional<Long> externalUserId, int money) {
		this.userId = userId;
		this.externalUserId = externalUserId.orElse(null); // Optional 처리
		this.money = money;
		this.externalTransfer = externalUserId.isPresent(); // 외부 사용자 송금 여부 설정
	}

	// 적금 계좌로 송금 생성자
	public TransferRequestDto(Long userId, Long savingsAccountId, int money) {
		this.userId = userId;
		this.savingsAccountId = savingsAccountId;
		this.money = money;
		this.externalTransfer = false; // 적금 계좌 송금
	}

	// 외부 송금 여부 확인
	public boolean isExternalTransfer() {
		return externalTransfer;
	}
}
