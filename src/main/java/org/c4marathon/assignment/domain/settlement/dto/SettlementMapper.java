package org.c4marathon.assignment.domain.settlement.dto;

import static org.c4marathon.assignment.domain.settlement.entity.settlement.SettlementRole.*;
import static org.c4marathon.assignment.domain.user.entity.UserErrCode.*;

import java.util.ArrayList;
import java.util.List;

import org.c4marathon.assignment.domain.settlement.dto.request.SettlementRequestDto;
import org.c4marathon.assignment.domain.settlement.dto.response.SettlementHistoryResponseDto;
import org.c4marathon.assignment.domain.settlement.entity.settlement.Settlement;
import org.c4marathon.assignment.domain.settlement.entity.settlement.SettlementStatus;
import org.c4marathon.assignment.domain.settlement.entity.settlement.SettlementUser;
import org.c4marathon.assignment.domain.settlement.repository.SettlementUserRepository;
import org.c4marathon.assignment.domain.user.entity.User;
import org.c4marathon.assignment.domain.user.exception.UserException;
import org.c4marathon.assignment.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SettlementMapper {
	private static SettlementUserRepository settlementUserRepository;
	private static UserRepository userRepository;

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

	public static Settlement toSettlement(SettlementRequestDto settlementRequestDto) {
		return Settlement.builder()
			.totalAmount(settlementRequestDto.settlementAmount())
			.settlementType(settlementRequestDto.settlementType())
			.numberOfUsers(settlementRequestDto.settlementTargetList().size())
			.remainingUsers(settlementRequestDto.settlementTargetList().size())
			.totalAmount(settlementRequestDto.settlementAmount())
			.settlementStatus(SettlementStatus.REQUESTED)
			.build();
	}
	
	public static void toSettlementUser(SettlementRequestDto settlementRequestDto, User user, Settlement settlement) {
		SettlementUser settlementUser = new SettlementUser(RECEIVER, user, settlement);
		settlementUserRepository.save(settlementUser);
		for (String senderPhone : settlementRequestDto.settlementTargetList()) {
			User sender = userRepository.findByUserPhone(senderPhone)
				.orElseThrow(() -> new UserException(USER_NOT_FOUND));
			SettlementUser settlementSender = new SettlementUser(SENDER, sender, settlement);
			settlementUserRepository.save(settlementSender);
		}
	}
}
