package org.c4marathon.assignment.domain;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
public class Settlement {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	//총 정산 금액
	private int totalAmount;

	//정산 요청 날짜
	private LocalDate requestDate;

	//정산 방식(1/n 또는 랜덤)
	@Enumerated(EnumType.STRING)
	private SettlementType settlementType;

	//참여자 리스트
	@ElementCollection
	private List<Long> participants;

	//각 사용자가 내야 할 금액
	@ElementCollection
	private List<Integer> amounts;

	// 정산 상태
	@Enumerated(EnumType.STRING)
	private SettlementStatus status;

	public Settlement(int totalAmount, LocalDate requestDate, SettlementType settlementType, List<Long> participants,
		List<Integer> amounts) {
		this.totalAmount = totalAmount;
		this.requestDate = requestDate;
		this.settlementType = settlementType;
		this.participants = participants;
		this.amounts = amounts;
		this.status = SettlementStatus.PENDING;  // 기본 상태는 PENDING
	}

	//상태를 완료로 변경하는 메서드
	public void completeSettlement() {
		this.status = SettlementStatus.COMPLETED;
	}
}
