package org.c4marathon.assignment.domain.account.entity.settlement;

import static jakarta.persistence.EnumType.*;

import java.util.List;

import org.c4marathon.assignment.domain.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
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
	@Column(name = "settlementTarget")
	private List<User> settlementTarget;
	@Column(name = "settlementStatus")
	@Enumerated(STRING)
	private SettlementStatus settlementStatus;
	@ManyToOne(fetch = FetchType.LAZY)
	private User user; // 정산 요청자
}
