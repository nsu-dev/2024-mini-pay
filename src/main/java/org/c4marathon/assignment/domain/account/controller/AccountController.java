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

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/account")
public class AccountController {
	private final AccountService accountService;

	@PostMapping("/remittance/{userId}")
	public ResponseEntity<RemittanceResponseDto> chargeMain(@RequestBody RemittanceRequestDto remittanceRequestDto,
		@PathVariable Long userId, HttpServletRequest httpServletRequest) {
		RemittanceResponseDto remittanceResponseDto = accountService.chargeMain(remittanceRequestDto, userId, httpServletRequest);
		return ResponseEntity.ok().body(remittanceResponseDto);
	}

	@PostMapping("/creataccount/{createAccountRole}/{userId}")
	public ResponseEntity<CreateResponseDto> createAccount(@RequestBody @PathVariable Long userId,
		@PathVariable String createAccountRole, HttpServletRequest httpServletRequest) {
		CreateResponseDto createResponseDto = accountService.createAccountOther(userId, createAccountRole, httpServletRequest);
		return ResponseEntity.ok().body(createResponseDto);
	}

	@PostMapping("/saving/{accountId}")
	public ResponseEntity<RemittanceResponseDto> savingRemittance(@PathVariable Long accountId, @RequestBody
	SavingRequestDto savingRequestDto, HttpServletRequest httpServletRequest) {
		RemittanceResponseDto remittanceResponseDto = accountService.savingRemittance(accountId, savingRequestDto, httpServletRequest);
		return ResponseEntity.ok().body(remittanceResponseDto);
	}
}
