package org.c4marathon.assignment.domain.account.entity.settlement;

import static jakarta.persistence.EnumType.*;

import java.util.ArrayList;
import java.util.List;

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
	@Column(name = "totalAmount")
	private Long totalAmount;        //정산 해야할 총 금액
	@Column(name = "settlementType")
	@Enumerated(STRING)
	private SettlementType settleType;
	@Column(name = "numberOfUsers")
	private int numberOfUsers;
	@Column(name = "remainingUsers")
	private int remainingUsers;
	@Column(name = "remainingAmount")
	private Long remainingAmount;
	@Column(name = "settlementStatus")
	@Enumerated(STRING)
	private SettlementStatus settlementStatus;
	@OneToMany(mappedBy = "settlement")
	private List<SettlementUser> settlementUserList = new ArrayList<>();

	@Builder
	private Settlement(Long totalAmount, SettlementType settlementType, int numberOfUsers,
		SettlementStatus settlementStatus) {
		this.totalAmount = totalAmount;
		this.settleType = settlementType;
		this.numberOfUsers = numberOfUsers;
		this.settlementStatus = settlementStatus;
	}
}
