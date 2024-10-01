package org.c4marathon.assignment.domain.settlement.dto;

import java.util.ArrayList;
import java.util.List;

import org.c4marathon.assignment.domain.settlement.dto.response.SettlementResponseDto;
import org.c4marathon.assignment.domain.settlement.entity.settlement.SettlementUser;

public class SettlementMapper {
	public static List<SettlementResponseDto> toSettlementResponseDtoList(List<SettlementUser> settlementUserList) {
		List<SettlementResponseDto> settlementResponseDtoList = new ArrayList<>();
		for (SettlementUser settlementUser : settlementUserList) {
			SettlementResponseDto settlementResponseDto = SettlementResponseDto.builder()
				.settlementId(settlementUser.getSettlement().getSettlementId())
				.totalAmount(settlementUser.getSettlement().getTotalAmount())
				.settlementType(settlementUser.getSettlement().getSettleType())
				.numberOfUsers(settlementUser.getSettlement().getNumberOfUsers())
				.remainingUsers(settlementUser.getSettlement().getRemainingUsers())
				.remainingAmount(settlementUser.getSettlement().getRemainingAmount())
				.settlementStatus(settlementUser.getSettlement().getSettlementStatus())
				.build();
			settlementResponseDtoList.add(settlementResponseDto);
		}
		return settlementResponseDtoList;
	}
}
