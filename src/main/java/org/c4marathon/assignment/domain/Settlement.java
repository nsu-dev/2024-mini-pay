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

	private int totalAmount;
	private LocalDate requestDate;
	@Enumerated(EnumType.STRING)
	private SettlementType settlementType;
	@ElementCollection
	private List<Long> participants;
	@ElementCollection
	private List<Integer> amounts;
	@Enumerated(EnumType.STRING)
	private SettlementStatus status;

	public Settlement(int totalAmount, LocalDate requestDate, SettlementType settlementType, List<Long> participants,
		List<Integer> amounts) {
		this.totalAmount = totalAmount;
		this.requestDate = requestDate;
		this.settlementType = settlementType;
		this.participants = participants;
		this.amounts = amounts;
		this.status = SettlementStatus.PENDING;
	}

	public void completeSettlement() {
		this.status = SettlementStatus.COMPLETED;
	}
}
