package org.c4marathon.assignment.controller;

import java.util.List;

import org.c4marathon.assignment.domain.Settlement;
import org.c4marathon.assignment.domain.SettlementType;
import org.c4marathon.assignment.service.SettlementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/settlements")
public class SettlementController {
	private final SettlementService settlementService;

	public SettlementController(SettlementService settlementService) {
		this.settlementService = settlementService;
	}

	// 정산 요청
	@PostMapping("/request")
	public ResponseEntity<Settlement> requestSettlement(
		@RequestParam int totalAmount,
		@RequestParam SettlementType type,
		@RequestBody List<Long> participants) {
		// 정산 요청 처리
		Settlement settlement = settlementService.requestSettlement(totalAmount, type, participants);
		return ResponseEntity.ok(settlement);
	}

	// 특정 정산 조회
	@GetMapping("/{id}")
	public ResponseEntity<Settlement> getSettlement(@PathVariable(value = "id") Long id) {
		Settlement settlement = settlementService.getSettlement(id);
		return ResponseEntity.ok(settlement);
	}

	// 정산 완료 처리
	@PutMapping("/{id}/complete")
	public ResponseEntity<Void> completeSettlement(@PathVariable(value = "id") Long id) {
		settlementService.completeSettlement(id);
		return ResponseEntity.noContent().build();  // 성공 시 204 No Content 반환
	}
}
