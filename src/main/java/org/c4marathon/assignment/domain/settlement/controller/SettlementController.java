package org.c4marathon.assignment.domain.settlement.controller;

import java.util.List;

import org.c4marathon.assignment.domain.account.dto.response.RemittanceResponseDto;
import org.c4marathon.assignment.domain.settlement.dto.request.SettlementRequestDto;
import org.c4marathon.assignment.domain.settlement.dto.response.SettlementHistoryResponseDto;
import org.c4marathon.assignment.domain.settlement.dto.response.SettlementResponseDto;
import org.c4marathon.assignment.domain.settlement.service.SettlementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping("/settlement")
public class SettlementController {
	private final SettlementService settlementService;

	//정산 목록 조회
	@GetMapping
	public ResponseEntity<List<SettlementHistoryResponseDto>> findAllSettlementList(
		HttpServletRequest httpServletRequest
	) {
		List<SettlementHistoryResponseDto> settlementHistoryResponseDtoList =
			settlementService.findAllSettlement(httpServletRequest);
		return ResponseEntity.ok().body(settlementHistoryResponseDtoList);
	}

	//정산 요청
	@PostMapping("/request")
	public ResponseEntity<SettlementResponseDto> requestSettlement(
		@RequestBody @Valid SettlementRequestDto settlementRequestDto,
		HttpServletRequest httpServletRequest
	) {
		SettlementResponseDto settlementResponseDto =
			settlementService.requestSettlement(settlementRequestDto, httpServletRequest);
		return ResponseEntity.ok().body(settlementResponseDto);
	}

	//정산수행
	@PostMapping("/{settlementUserId}")
	public ResponseEntity<RemittanceResponseDto> performSettlement(
		@PathVariable Long settlementUserId,
		HttpServletRequest httpServletRequest
	) {
		RemittanceResponseDto remittanceResponseDto =
			settlementService.settlementSplit(settlementUserId, httpServletRequest);
		return ResponseEntity.ok().body(remittanceResponseDto);
	}
}
