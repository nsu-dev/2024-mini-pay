package org.c4marathon.assignment.Dto;

import lombok.Getter;

@Getter
public class TransferRequest {
	private Long userId;
	private Long savingsAccountId;
	private int money;

	public TransferRequest(Long userId, Long savingsAccountId, int money) {
		this.userId = userId;
		this.savingsAccountId = savingsAccountId;
		this.money = money;
	}
}
