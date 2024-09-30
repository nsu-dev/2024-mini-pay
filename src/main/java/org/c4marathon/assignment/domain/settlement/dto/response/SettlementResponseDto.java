package org.c4marathon.assignment.domain.settlement.dto.response;

import org.c4marathon.assignment.domain.settlement.entity.settlement.SettlementStatus;
import org.c4marathon.assignment.domain.settlement.entity.settlement.SettlementType;

public class SettlementResponseDto {
	private Long settlementId;
	private Long totalAmount;
	private SettlementType settlementType;
	private int numberOfUsers;
	private int remainingUsers;
	private Long remainingAmount;
	private SettlementStatus settlementStatus;
}
