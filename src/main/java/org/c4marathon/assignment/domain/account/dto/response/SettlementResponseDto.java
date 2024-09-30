package org.c4marathon.assignment.domain.account.dto.response;

import org.c4marathon.assignment.domain.account.entity.settlement.SettlementStatus;
import org.c4marathon.assignment.domain.account.entity.settlement.SettlementType;

public class SettlementResponseDto {
	private Long settlementId;
	private Long totalAmount;
	private SettlementType settlementType;
	private int numberOfUsers;
	private int remainingUsers;
	private Long remainingAmount;
	private SettlementStatus settlementStatus;
}
