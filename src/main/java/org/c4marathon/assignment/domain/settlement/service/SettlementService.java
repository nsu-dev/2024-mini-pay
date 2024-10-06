package org.c4marathon.assignment.domain.settlement.service;

import static org.c4marathon.assignment.domain.settlement.entity.responsemsg.SettlementErrCode.*;
import static org.c4marathon.assignment.domain.settlement.entity.settlement.SettlementRole.*;
import static org.c4marathon.assignment.domain.user.entity.UserErrCode.*;

import java.util.List;

import org.c4marathon.assignment.domain.account.dto.request.RemittanceRequestDto;
import org.c4marathon.assignment.domain.account.dto.response.RemittanceResponseDto;
import org.c4marathon.assignment.domain.account.entity.account.Account;
import org.c4marathon.assignment.domain.account.entity.account.AccountRole;
import org.c4marathon.assignment.domain.account.repository.AccountRepository;
import org.c4marathon.assignment.domain.account.service.AccountService;
import org.c4marathon.assignment.domain.account.transaction.TransactionHandler;
import org.c4marathon.assignment.domain.settlement.dto.SettlementMapper;
import org.c4marathon.assignment.domain.settlement.dto.request.SettlementRequestDto;
import org.c4marathon.assignment.domain.settlement.dto.response.SettlementHistoryResponseDto;
import org.c4marathon.assignment.domain.settlement.dto.response.SettlementResponseDto;
import org.c4marathon.assignment.domain.settlement.entity.responsemsg.SettlementResponseMsg;
import org.c4marathon.assignment.domain.settlement.entity.settlement.Settlement;
import org.c4marathon.assignment.domain.settlement.entity.settlement.SettlementUser;
import org.c4marathon.assignment.domain.settlement.exception.SettlementException;
import org.c4marathon.assignment.domain.settlement.repository.SettlementUserRepository;
import org.c4marathon.assignment.domain.user.entity.User;
import org.c4marathon.assignment.domain.user.exception.UserException;
import org.c4marathon.assignment.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SettlementService {
	private SettlementUserRepository settlementUserRepository;
	private UserRepository userRepository;
	private AccountRepository accountRepository;
	private AccountService accountService;
	private TransactionHandler transactionHandler;

	//정산 목록 불러오는 메서드
	public List<SettlementHistoryResponseDto> findAllSettlement(HttpServletRequest httpServletRequest) {
		Long userId = getSessionId(httpServletRequest);
		User user = userRepository.findById(userId).orElseThrow(() -> new UserException(USER_NOT_FOUND));
		List<SettlementUser> settlementUserList = settlementUserRepository.findAllByUser(user)
			.orElseThrow(() -> new SettlementException(SETTLEMENT_NOT_FOUND));
		return SettlementMapper.toSettlementResponseDtoList(settlementUserList);
	}

	//정산 요청 메서드
	public SettlementResponseDto requestSettlement(
		SettlementRequestDto settlementRequestDto,
		HttpServletRequest httpServletRequest
	) {
		Settlement settlement = SettlementMapper.toSettlement(settlementRequestDto);
		Long userId = getSessionId(httpServletRequest);
		User user = userRepository.findById(userId).orElseThrow(() -> new UserException(USER_NOT_FOUND));
		SettlementMapper.toSettlementUser(settlementRequestDto, user, settlement);
		return new SettlementResponseDto(SettlementResponseMsg.REQUEST_COMPLETED.getResponseMsg());
	}

	//정산 시작 메서드
	public RemittanceResponseDto settlementSplit(
		Long settlementUserId,
		HttpServletRequest httpServletRequest
	) {
		SettlementUser settlementUser = settlementUserRepository.findById(settlementUserId)
			.orElseThrow(() -> new SettlementException(SETTLEMENT_NOT_FOUND));
		User receiver = settlementUserRepository.findReceiver(RECEIVER, settlementUser.getSettlement());
		return determineSettlementType(settlementUser, receiver, httpServletRequest);
	}

	//정산 유형 판별 후 정산 수행하는 메서드를 호출하는 메서드
	public RemittanceResponseDto determineSettlementType(
		SettlementUser settlementUser,
		User receiver,
		HttpServletRequest httpServletRequest
	) {
		RemittanceRequestDto remittanceRequestDto;
		Settlement settlement = settlementUser.getSettlement();
		switch (settlementUser.getSettlement().getSettleType()) {
			case EQUALS -> {
				remittanceRequestDto = settlementSplitEquals(settlementUser.getSettlement(), receiver);
			}
			case RANDOM -> {
				remittanceRequestDto = settlementSplitRandom(settlementUser.getSettlement(), receiver);
			}
			default -> throw new SettlementException(SETTLEMENT_INVALID_TYPE);
		}
		//테스트를 통해서 delete가 바로 적용되는지 알아보기
		transactionHandler.runInCommittedTransaction(() -> {
			settlementUserRepository.deleteById(settlementUser.getSettlementUserId());
			settlement.updateRemainingUsers(settlementUserRepository.countRemainingUsers(settlement));
		});
		return accountService.remittanceOtherMain(remittanceRequestDto, httpServletRequest);
	}

	//정산 수행 메서드(n/1)
	private RemittanceRequestDto settlementSplitEquals(Settlement settlement, User receiver) {
		int remainingUsers = settlement.getRemainingUsers();
		Long remainingAmount = settlement.getRemainingAmount();
		Long remittanceAmount = calculateSplitEquals(remainingUsers, remainingAmount);
		transactionHandler.runInCommittedTransaction(() -> {
			settlement.updateRemainingAmount(remittanceAmount);
		});
		Account account = accountRepository.findMainAccount(receiver.getUserId(), AccountRole.MAIN);
		return new RemittanceRequestDto(account.getAccountNum(), remittanceAmount);
	}

	//정산 수행 메서드(랜덤)
	private RemittanceRequestDto settlementSplitRandom(Settlement settlement, User receiver) {
		int remainingUsers = settlement.getRemainingUsers();
		Long remainingAmount = settlement.getRemainingAmount();
		Long remittanceAmount = calculateSplitRandom(remainingUsers, remainingAmount);
		settlement.updateRemainingAmount(remittanceAmount);
		Account account = accountRepository.findMainAccount(receiver.getUserId(), AccountRole.MAIN);
		return new RemittanceRequestDto(account.getAccountNum(), remittanceAmount);
	}

	// n/1 정산 금액 계산
	// 먼저 정산하는 사람이 남는 금액의 1원을 더 부담하도록 하여 총 합이 총 정산 금액에 맞도록 계산
	// 남는 금액이 정산하는 사람 수 보다 클 수 없어서 1원씩만 더 부담하면 됨.
	private Long calculateSplitEquals(int remainingUsers, Long remainingAmount) {
		if (remainingUsers == 1) {
			return remainingAmount;
		}
		long splitRemittanceAmount = remainingAmount / remainingUsers;
		if (remainingAmount % remainingUsers != 0) {
			return splitRemittanceAmount + 1L;
		}
		return splitRemittanceAmount;
	}

	// 랜덤 정산 금액 계산
	// 최소는 1원 최대는 평균 정산금액의 2배
	private Long calculateSplitRandom(int remainingUsers, Long remainingAmount) {
		if (remainingUsers == 1) {
			return remainingAmount;
		}
		long minAmount = 1L;
		long maxAmount = remainingAmount / remainingUsers * 2; // 한 사용자에게 배정 가능한 최대 금액
		return (long)(Math.random() * (maxAmount - minAmount + 1)) + minAmount;
	}

	private Long getSessionId(HttpServletRequest httpServletRequest) {
		HttpSession session = httpServletRequest.getSession(false);
		if (session == null || session.getAttribute("userId") == null) {
			throw new UserException(USER_SESSION_ERR);
		}
		return (Long)session.getAttribute("userId");
	}
}
