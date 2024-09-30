package org.c4marathon.assignment.account.controller;

import org.c4marathon.assignment.account.dto.ChargeDto;
import org.c4marathon.assignment.account.dto.SavingAccountPwDto;
import org.c4marathon.assignment.account.dto.SendDto;
import org.c4marathon.assignment.account.service.AccountService;
import org.c4marathon.assignment.config.CommonResponse;
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

		boolean checkCharge = accountService.chargeMainAccount(chargeDto);

		if (checkCharge) {
			CommonResponse res = new CommonResponse(
				200,
				HttpStatus.OK,
				"충전 성공",
				null
			);
			return new ResponseEntity<>(res, res.getHttpStatus());
		} else {
			CommonResponse res = new CommonResponse(
				400,
				HttpStatus.BAD_REQUEST,
				"충전 실패",
				null
			);
			return new ResponseEntity<>(res, res.getHttpStatus());
		}
	}

	// 적금 계좌 생성
	@PostMapping("/account/create/{userId}")
	public ResponseEntity<CommonResponse> CreateSavingsAccount(@PathVariable("userId") String userId,
		@Valid @RequestBody SavingAccountPwDto savingAccountPwDto) {
		boolean checkSaving = accountService.createSavingAccount(userId, savingAccountPwDto);

		if (checkSaving) {
			CommonResponse res = new CommonResponse(
				200,
				HttpStatus.OK,
				"적금계좌 생성완료",
				null
			);
			return new ResponseEntity<>(res, res.getHttpStatus());
		} else {
			CommonResponse res = new CommonResponse(
				400,
				HttpStatus.BAD_REQUEST,
				"적금계좌 생성실패",
				null
			);
			return new ResponseEntity<>(res, res.getHttpStatus());
		}
	}

	// 적금 계좌로 돈 송금
	@PostMapping("/account/send/saving")
	public ResponseEntity<CommonResponse> sendMoney(@Valid @RequestBody SendDto sendDto) {
		boolean checkSend = accountService.sendSavingAccount(sendDto);

		if (checkSend) {
			CommonResponse res = new CommonResponse(
				200,
				HttpStatus.OK,
				"송금완료",
				null
			);
			return new ResponseEntity<>(res, res.getHttpStatus());
		} else {
			CommonResponse res = new CommonResponse(
				400,
				HttpStatus.BAD_REQUEST,
				"송금실패",
				null
			);
			return new ResponseEntity<>(res, res.getHttpStatus());
		}
	}

	// 다른 계좌로 돈 송금
	@PostMapping("/account/send/other")
	public ResponseEntity<CommonResponse> otherSendMoney(@Valid @RequestBody SendDto sendDto) {
		boolean checkSend = accountService.sendOtherAccount(sendDto);

		if (checkSend) {
			CommonResponse res = new CommonResponse(
				200,
				HttpStatus.OK,
				"송금완료",
				null
			);
			return new ResponseEntity<>(res, res.getHttpStatus());
		} else {
			CommonResponse res = new CommonResponse(
				400,
				HttpStatus.BAD_REQUEST,
				"송금실패",
				null
			);
			return new ResponseEntity<>(res, res.getHttpStatus());
		}
	}

}
