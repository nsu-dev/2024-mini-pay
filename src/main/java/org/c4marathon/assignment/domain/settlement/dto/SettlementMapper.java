package org.c4marathon.assignment.domain.settlement.dto;

import java.util.ArrayList;
import java.util.List;

import org.c4marathon.assignment.domain.settlement.dto.response.SettlementHistoryResponseDto;
import org.c4marathon.assignment.domain.settlement.entity.settlement.SettlementUser;

public class SettlementMapper {
	public static List<SettlementHistoryResponseDto> toSettlementResponseDtoList(
		List<SettlementUser> settlementUserList) {
		List<SettlementHistoryResponseDto> settlementHistoryResponseDtoList = new ArrayList<>();
		for (SettlementUser settlementUser : settlementUserList) {
			SettlementHistoryResponseDto settlementHistoryResponseDto = SettlementHistoryResponseDto.builder()
				.settlementId(settlementUser.getSettlement().getSettlementId())
				.totalAmount(settlementUser.getSettlement().getTotalAmount())
				.settlementType(settlementUser.getSettlement().getSettleType())
				.numberOfUsers(settlementUser.getSettlement().getNumberOfUsers())
				.remainingUsers(settlementUser.getSettlement().getRemainingUsers())
				.remainingAmount(settlementUser.getSettlement().getRemainingAmount())
				.settlementStatus(settlementUser.getSettlement().getSettlementStatus())
				.build();
			settlementHistoryResponseDtoList.add(settlementHistoryResponseDto);
		}
		return settlementHistoryResponseDtoList;
	}
}
