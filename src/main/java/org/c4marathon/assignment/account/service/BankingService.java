package org.c4marathon.assignment.account.service;

import org.c4marathon.assignment.account.dto.request.SendToOthersRequestDto;
import org.c4marathon.assignment.account.dto.response.SendToOthersResponseDto;
import org.c4marathon.assignment.user.domain.User;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BankingService {

	private final AccountService accountService;

	public SendToOthersResponseDto sendToOthers(
		Long othersAccountId,
		User user,
		SendToOthersRequestDto requestDto
	) {
		int sendToMoney = accountService.withdrawal(user, requestDto);
		return accountService.deposit(othersAccountId, sendToMoney, requestDto);
	}
}
