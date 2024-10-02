package org.c4marathon.assignment.account.controller;

import org.c4marathon.assignment.account.dto.ChargeDto;
import org.c4marathon.assignment.account.dto.SavingAccountPwDto;
import org.c4marathon.assignment.account.dto.SendDto;
import org.c4marathon.assignment.account.service.AccountService;
import org.c4marathon.assignment.common.config.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AccountController {

	private final AccountService accountService;

	// 메인 계좌에 돈 충전
	@PostMapping("/account/charge")
	public ResponseEntity<CommonResponse> chargingMoney(@Valid @RequestBody ChargeDto chargeDto) {

		accountService.chargeMainAccount(chargeDto);

		CommonResponse res = new CommonResponse(
			200,
			HttpStatus.OK,
			"충전 성공",
			null
		);
		return new ResponseEntity<>(res, res.getHttpStatus());
	}

	// 적금 계좌 생성
	@PostMapping("/account/create/{userId}")
	public ResponseEntity<CommonResponse> CreateSavingsAccount(@PathVariable("userId") String userId,
		@Valid @RequestBody SavingAccountPwDto savingAccountPwDto) {
		accountService.craeteSavingAccount(userId, savingAccountPwDto);

		CommonResponse res = new CommonResponse(
			200,
			HttpStatus.OK,
			"적금계좌 생성완료",
			null
		);
		return new ResponseEntity<>(res, res.getHttpStatus());
	}

	// 적금 계좌로 돈 송금
	@PostMapping("/account/send/saving/{userId}")
	public ResponseEntity<CommonResponse> sendMoneyToSaving(@PathVariable("userId") @RequestBody String userId,
		@Valid @RequestBody SendDto sendDto) {
		accountService.sendSavingAccount(userId, sendDto);

		CommonResponse res = new CommonResponse(
			200,
			HttpStatus.OK,
			"송금완료",
			null
		);
		return new ResponseEntity<>(res, res.getHttpStatus());
	}

	// 다른 사람의 메인 계좌로 돈 송금
	@PostMapping("/account/send/other/{userId}")
	public ResponseEntity<CommonResponse> sendMoneyToOther(@PathVariable("userId") @RequestBody String userId,
		@Valid @RequestBody SendDto sendDto) {
		accountService.sendOtherAccount(userId, sendDto);

		CommonResponse res = new CommonResponse(
			200,
			HttpStatus.OK,
			"송금완료",
			null
		);
		return new ResponseEntity<>(res, res.getHttpStatus());
	}

}
