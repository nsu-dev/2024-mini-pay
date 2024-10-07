package org.c4marathon.assignment.account.controller;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.c4marathon.assignment.account.dto.CalculatePaymentDto;
import org.c4marathon.assignment.account.dto.ChargeDto;
import org.c4marathon.assignment.account.dto.SavingAccountPwDto;
import org.c4marathon.assignment.account.dto.SendDto;
import org.c4marathon.assignment.account.dto.SettlementDto;
import org.c4marathon.assignment.account.service.AccountService;
import org.c4marathon.assignment.common.config.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AccountController {

	// 각 사용자의 SSE 연결을 관리할 Map (userId를 키로 사용)
	private final Map<String, SseEmitter> sseEmitters = new ConcurrentHashMap<>();

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

	// SSE 구독
	public SseEmitter subscribe(@RequestParam String userId) {
		SseEmitter emitter = new SseEmitter(0L); // 타임아웃 없음
		sseEmitters.put(userId, emitter);

		// SSE 연결이 완료되면 Map에서 제거
		emitter.onCompletion(() -> sseEmitters.remove(userId));
		emitter.onTimeout(() -> sseEmitters.remove(userId));

		return emitter;
	}

	// SSE 이벤트 전송
	public void sendEventToClient(String userId, String eventName, Enum type) {
		SseEmitter emitter = sseEmitters.get(userId);
		if (emitter != null) {
			try {
				emitter.send(SseEmitter.event()
					.name(eventName)
					.data(type));
			} catch (IOException e) {
				sseEmitters.remove(userId); // 전송 실패 시 Map에서 제거
			}
		}
	}

	// 1명의 사용자가 n명의 사용자에게 정산 요청
	@PostMapping("/account/settlement/request")
	public ResponseEntity<CommonResponse> requestSettlement(@RequestBody CalculatePaymentDto calculatePaymentDto) {

		// 자동 구독
		for (String participantUserId : calculatePaymentDto.usersId()) {
			subscribe(participantUserId);
		}

		accountService.requestSettlement(calculatePaymentDto);

		CommonResponse res = new CommonResponse(200, HttpStatus.OK, "정산 요청 완료", null);
		return new ResponseEntity<>(res, res.getHttpStatus());
	}

	// n명이 정산 요청을 수락하고 정산 진행
	@PostMapping("/account/settlement/process")
	public ResponseEntity<CommonResponse> processSettlement(@RequestBody SettlementDto settlementDto) {

		// 자동 구독
		for (String participantUserId : settlementDto.usersId()) {
			subscribe(participantUserId);
		}

		accountService.processSettlement(settlementDto);

		CommonResponse res = new CommonResponse(200, HttpStatus.OK, "정산 완료", null);
		return new ResponseEntity<>(res, res.getHttpStatus());
	}

}
