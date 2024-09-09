package org.c4marathon.assignment.account.controller;

import org.c4marathon.assignment.account.dto.response.SavingAccountResponseDto;
import org.c4marathon.assignment.account.service.AccountService;
import org.c4marathon.assignment.user.domain.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
