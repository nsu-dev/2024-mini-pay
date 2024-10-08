package org.c4marathon.assignment.domain.settlement.dto.response;

import org.c4marathon.assignment.domain.settlement.entity.settlement.SettlementStatus;
import org.c4marathon.assignment.domain.settlement.entity.settlement.SettlementType;

import lombok.Builder;

public class SettlementHistoryResponseDto {
	private Long settlementId;
	private Long totalAmount;
	private SettlementType settlementType;
	private int numberOfUsers;
	private int remainingUsers;
	private Long remainingAmount;
	private SettlementStatus settlementStatus;

	@Builder
	SettlementHistoryResponseDto(Long settlementId, Long totalAmount, SettlementType settlementType, int numberOfUsers,
		int remainingUsers, Long remainingAmount, SettlementStatus settlementStatus) {
		this.settlementId = settlementId;
		this.totalAmount = totalAmount;
		this.settlementType = settlementType;
		this.numberOfUsers = numberOfUsers;
		this.remainingUsers = remainingUsers;
		this.remainingAmount = remainingAmount;
		this.settlementStatus = settlementStatus;
	}
}
