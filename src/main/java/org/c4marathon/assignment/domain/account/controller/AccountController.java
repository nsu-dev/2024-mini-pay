package org.c4marathon.assignment.domain.account.controller;

import org.c4marathon.assignment.domain.account.dto.RemittanceRequestDto;
import org.c4marathon.assignment.domain.account.dto.RemittanceResponseDto;
import org.c4marathon.assignment.domain.account.entity.RemittanceResponseMsg;
import org.c4marathon.assignment.domain.account.service.AccountService;
import org.springframework.http.ResponseEntity;
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
			if (remittanceResponseDto.responseMsg() == RemittanceResponseMsg.SUCCESS.getResponseMsg()) {
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
}
