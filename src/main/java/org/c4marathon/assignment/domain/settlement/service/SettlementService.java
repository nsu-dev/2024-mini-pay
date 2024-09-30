package org.c4marathon.assignment.domain.settlement;

import static org.c4marathon.assignment.domain.user.entity.UserErrCode.*;

import java.util.List;

import org.c4marathon.assignment.domain.settlement.entity.SettlementUser;
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

	public List<SettlementResponseDto> findAllSettlement(HttpServletRequest httpServletRequest) {
		Long userId = getSessionId(httpServletRequest);
		User user = userRepository.findById(userId).orElseThrow(() -> new UserException(USER_NOT_FOUND));
		List<SettlementUser> settlementUserList = settlementUserRepository.findAllByUser(user).orElseThrow();
	}

	private Long getSessionId(HttpServletRequest httpServletRequest) {
		HttpSession session = httpServletRequest.getSession(false);
		if (session == null || session.getAttribute("userId") == null) {
			throw new UserException(USER_SESSION_ERR);
		}
		return (Long)session.getAttribute("userId");
	}
}
