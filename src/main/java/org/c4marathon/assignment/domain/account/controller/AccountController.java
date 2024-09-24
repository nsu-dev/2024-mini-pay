package org.c4marathon.assignment.domain.account.controller;

import org.c4marathon.assignment.domain.account.dto.CreateResponseDto;
import org.c4marathon.assignment.domain.account.dto.RemittanceRequestDto;
import org.c4marathon.assignment.domain.account.dto.RemittanceResponseDto;
import org.c4marathon.assignment.domain.account.dto.SavingRequestDto;
import org.c4marathon.assignment.domain.account.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/account")
public class AccountController {
	private final AccountService accountService;

	//메인계좌 충전 api
	@PostMapping("/remittance")
	public ResponseEntity<RemittanceResponseDto> chargeMain(@RequestBody @Valid RemittanceRequestDto remittanceRequestDto) {
		RemittanceResponseDto remittanceResponseDto = accountService.chargeMain(remittanceRequestDto);
		return ResponseEntity.ok().body(remittanceResponseDto);
	}

	//메인계좌 외 계좌 생성 api
	@PostMapping("/creataccount/{createAccountRole}")
	public ResponseEntity<CreateResponseDto> createAccount(@PathVariable String createAccountRole,
		HttpServletRequest httpServletRequest) {
		CreateResponseDto createResponseDto = accountService.createAccountOther(createAccountRole, httpServletRequest);
		return ResponseEntity.ok().body(createResponseDto);
	}

	//적금계좌 <- 메인계좌로의 송금 api
	@PostMapping("/saving/{accountId}")
	public ResponseEntity<RemittanceResponseDto> savingRemittance(@PathVariable Long accountId,
		@RequestBody SavingRequestDto savingRequestDto, HttpServletRequest httpServletRequest) {
		RemittanceResponseDto remittanceResponseDto = accountService.savingRemittance(accountId, savingRequestDto, httpServletRequest);
		return ResponseEntity.ok().body(remittanceResponseDto);
	}

	//메인계좌간의 송금api
	@PostMapping("/remittance/other")
	public ResponseEntity<RemittanceResponseDto> remittanceMain(@RequestBody @Valid RemittanceRequestDto remittanceRequestDto,
		HttpServletRequest httpServletRequest){
		RemittanceResponseDto remittanceResponseDto = accountService.remittanceOtherMain(remittanceRequestDto, httpServletRequest);
		return ResponseEntity.ok().body(remittanceResponseDto);
	}
}
