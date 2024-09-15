package org.c4marathon.assignment.domain.account.controller;

import static org.c4marathon.assignment.domain.account.entity.AccountErrCode.*;

import org.c4marathon.assignment.domain.account.dto.CreateResponseDto;
import org.c4marathon.assignment.domain.account.dto.RemittanceRequestDto;
import org.c4marathon.assignment.domain.account.dto.RemittanceResponseDto;
import org.c4marathon.assignment.domain.account.dto.SavingRequestDto;
import org.c4marathon.assignment.domain.account.exception.AccountException;
import org.c4marathon.assignment.domain.account.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/account")
public class AccountController {
	private final AccountService accountService;

	@PostMapping("/remittance")
	public ResponseEntity<RemittanceResponseDto> chargeMain(@RequestBody RemittanceRequestDto remittanceRequestDto) {
		try {
			RemittanceResponseDto remittanceResponseDto = accountService.chargeMain(remittanceRequestDto);
			return ResponseEntity.ok().body(remittanceResponseDto);
		} catch (RuntimeException e) {
			throw new AccountException(ACCOUNT_SERVER_ERROR);
		}
	}

	@PostMapping("/creataccount/{createAccountRole}/{userId}")
	public ResponseEntity<CreateResponseDto> createAccount(@RequestBody @PathVariable Long userId,
		@PathVariable String createAccountRole) {
		try {
			CreateResponseDto createResponseDto = accountService.createAccountOther(userId, createAccountRole);
			return ResponseEntity.ok().body(createResponseDto);
		} catch (RuntimeException e) {
			throw new AccountException(ACCOUNT_SERVER_ERROR);
		}
	}

	@PostMapping("/saving/{accountId}")
	public ResponseEntity<RemittanceResponseDto> savingRemittance(@PathVariable Long accountId, @RequestBody
	SavingRequestDto savingRequestDto) {
		try {
			RemittanceResponseDto remittanceResponseDto = accountService.savingRemittance(accountId, savingRequestDto);
			return ResponseEntity.ok().body(remittanceResponseDto);
		} catch (RuntimeException e) {
			throw new AccountException(ACCOUNT_SERVER_ERROR);
		}
	}
}
