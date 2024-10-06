package org.c4marathon.assignment.domain.settlement.service;

import static org.c4marathon.assignment.domain.settlement.entity.responsemsg.SettlementErrCode.*;
import static org.c4marathon.assignment.domain.user.entity.UserErrCode.*;

import java.util.List;

import org.c4marathon.assignment.domain.settlement.dto.SettlementMapper;
import org.c4marathon.assignment.domain.settlement.dto.request.SettlementRequestDto;
import org.c4marathon.assignment.domain.settlement.dto.response.SettlementHistoryResponseDto;
import org.c4marathon.assignment.domain.settlement.dto.response.SettlementResponseDto;
import org.c4marathon.assignment.domain.settlement.entity.responsemsg.SettlementResponseMsg;
import org.c4marathon.assignment.domain.settlement.entity.settlement.Settlement;
import org.c4marathon.assignment.domain.settlement.entity.settlement.SettlementUser;
import org.c4marathon.assignment.domain.settlement.exception.SettlementException;
import org.c4marathon.assignment.domain.settlement.repository.SettlementRepository;
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
	private SettlementRepository settlementRepository;

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

	private Long getSessionId(HttpServletRequest httpServletRequest) {
		HttpSession session = httpServletRequest.getSession(false);
		if (session == null || session.getAttribute("userId") == null) {
			throw new UserException(USER_SESSION_ERR);
		}
		return (Long)session.getAttribute("userId");
	}
}
