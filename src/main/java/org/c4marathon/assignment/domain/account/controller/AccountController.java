package org.c4marathon.assignment.domain.account.controller;

import java.util.NoSuchElementException;

import org.c4marathon.assignment.domain.account.dto.CreateResponseDto;
import org.c4marathon.assignment.domain.account.dto.RemittanceRequestDto;
import org.c4marathon.assignment.domain.account.dto.RemittanceResponseDto;
import org.c4marathon.assignment.domain.account.dto.SavingRequestDto;
import org.c4marathon.assignment.domain.account.entity.CreateResponseMsg;
import org.c4marathon.assignment.domain.account.entity.RemittanceResponseMsg;
import org.c4marathon.assignment.domain.account.repository.AccountRepository;
import org.c4marathon.assignment.domain.account.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

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
			if (remittanceResponseDto.responseMsg().equals(RemittanceResponseMsg.SUCCESS.getResponseMsg())) {
				return ResponseEntity.ok().body(remittanceResponseDto);
			} else {
				return ResponseEntity.badRequest().body(remittanceResponseDto);
			}
		} catch (HttpClientErrorException e) {
			RemittanceResponseDto remittanceResponseDto = RemittanceResponseDto.builder()
				.responseMsg(RemittanceResponseMsg.NOSUCHACCOUNT.getResponseMsg())
				.build();
			return ResponseEntity.status(e.getStatusCode()).body(remittanceResponseDto);
		}

	}

	@PostMapping("/creataccount/{createAccountRole}/{userId}")
	public ResponseEntity<CreateResponseDto> createAccount(@RequestBody @PathVariable Long userId,
		@PathVariable String createAccountRole) {
		try {
			CreateResponseDto createResponseDto = accountService.createAccountOther(userId, createAccountRole);
			if (createResponseDto.responseMsg() == CreateResponseMsg.SUCCESS.getResponseMsg()) {
				return ResponseEntity.ok().body(createResponseDto);
			} else {
				return ResponseEntity.badRequest().body(createResponseDto);
			}
		} catch (NoSuchElementException e) {
			CreateResponseDto createResponseDto = CreateResponseDto.builder()
				.responseMsg(CreateResponseMsg.NOUSER.getResponseMsg())
				.build();
			return ResponseEntity.status(400).body(createResponseDto);
		}
	}

	@PostMapping("/saving/{accountId}")
	public ResponseEntity<RemittanceResponseDto> savingRemittance(@PathVariable Long accountId, @RequestBody
	SavingRequestDto savingRequestDto) {
		try {
			RemittanceResponseDto remittanceResponseDto = accountService.savingRemittance(accountId, savingRequestDto);
			return ResponseEntity.ok().body(remittanceResponseDto);
		} catch (NoSuchElementException e) {
			RemittanceResponseDto remittanceResponseDto = RemittanceResponseDto.builder()
				.responseMsg(RemittanceResponseMsg.NOSUCHACCOUNT.getResponseMsg())
				.build();
			return ResponseEntity.badRequest().body(remittanceResponseDto);
		} catch (HttpClientErrorException e) {
			RemittanceResponseDto remittanceResponseDto = RemittanceResponseDto.builder()
				.responseMsg(RemittanceResponseMsg.INSUFFICIENT_BALANCE.getResponseMsg())
				.build();
			return ResponseEntity.badRequest().body(remittanceResponseDto);
		}
	}
}
