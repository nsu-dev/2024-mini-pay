package org.c4marathon.assignment.account.controller;

import java.util.List;

import org.c4marathon.assignment.account.dto.request.ChargeRequestDto;
import org.c4marathon.assignment.account.dto.request.SendRequestDto;
import org.c4marathon.assignment.account.dto.response.AccountResponseDto;
import org.c4marathon.assignment.account.dto.response.ChargeResponseDto;
import org.c4marathon.assignment.account.dto.response.SavingAccountResponseDto;
import org.c4marathon.assignment.account.dto.response.SendResponseDto;
import org.c4marathon.assignment.account.service.AccountService;
import org.c4marathon.assignment.user.domain.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AccountController {

	private final AccountService accountService;

	@PostMapping("/api/user/saving-account")
	public ResponseEntity<SavingAccountResponseDto> generateSavingAccount(
		@AuthenticationPrincipal User user
	) {
		SavingAccountResponseDto savingAccountResponseDto = accountService.generateSavingAccount(user);
		return ResponseEntity.ok(savingAccountResponseDto);
	}

	@GetMapping("/api/accounts")
	public ResponseEntity<List<AccountResponseDto>> getAccounts(
		@AuthenticationPrincipal User user
	) {
		List<AccountResponseDto> accountResponseDto = accountService.getAccounts(user);
		return ResponseEntity.ok(accountResponseDto);
	}

	@PostMapping("/api/send")
	public ResponseEntity<SendResponseDto> sendMoney(
		@AuthenticationPrincipal User user,
		@Valid @RequestBody SendRequestDto requestDto) {

		SendResponseDto sendResponseDto = accountService.sendMoney(user, requestDto);
		return ResponseEntity.ok(sendResponseDto);
	}

	@PostMapping("/api/account/charge")
	public ResponseEntity<ChargeResponseDto> chargeMainAccount(
		@AuthenticationPrincipal User user,
		@Valid @RequestBody ChargeRequestDto requestDto) {

		ChargeResponseDto chargeResponseDto = accountService.chargeMainAccount(user, requestDto);
		return ResponseEntity.ok(chargeResponseDto);
	}

}
