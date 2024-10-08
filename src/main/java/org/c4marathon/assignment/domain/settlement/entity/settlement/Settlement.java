package org.c4marathon.assignment.domain.settlement.entity.settlement;

import static jakarta.persistence.EnumType.*;
import static org.c4marathon.assignment.domain.settlement.entity.settlement.SettlementStatus.*;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "settlement")
public class Settlement {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long settlementId;
	@Column(name = "totalAmount", nullable = false)
	private Long totalAmount;        //정산 해야할 총 금액
	@Column(name = "settlementType", nullable = false)
	@Enumerated(STRING)
	private SettlementType settleType;
	@Column(name = "numberOfUsers", nullable = false)
	private int numberOfUsers;
	@Column(name = "remainingUsers", nullable = false)
	private int remainingUsers;
	@Column(name = "remainingAmount", nullable = false)
	private Long remainingAmount;
	@Column(name = "settlementStatus", nullable = false)
	@Enumerated(STRING)
	private SettlementStatus settlementStatus;
	@OneToMany(mappedBy = "settlement", cascade = CascadeType.REMOVE)
	private List<SettlementUser> settlementUserList = new ArrayList<>();

	@Builder
	private Settlement(Long totalAmount,
		SettlementType settlementType, int numberOfUsers,
		int remainingUsers, Long remainingAmount,
		SettlementStatus settlementStatus) {
		this.totalAmount = totalAmount;
		this.settleType = settlementType;
		this.numberOfUsers = numberOfUsers;
		this.remainingUsers = remainingUsers;
		this.remainingAmount = remainingAmount;
		this.settlementStatus = settlementStatus;
	}

	public void updateRemainingAmount(Long remittanceAmount) {
		this.remainingAmount -= remittanceAmount;
		if (remittanceAmount == 0) {
			updateSettlementStatus(COMPLETED);
		}
	}

	public void updateRemainingUsers(int remainingUsers) {
		this.remainingUsers = remainingUsers;
	}

	private void updateSettlementStatus(SettlementStatus settlementStatus) {
		this.settlementStatus = settlementStatus;
	}
}
