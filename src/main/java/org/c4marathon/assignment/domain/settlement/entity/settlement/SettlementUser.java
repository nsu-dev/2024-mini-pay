package org.c4marathon.assignment.domain.settlement.entity.settlement;

import static jakarta.persistence.EnumType.*;

import org.c4marathon.assignment.domain.user.entity.user.User;

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
@Table(name = "settlement_user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class SettlementUser {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long settlementUserId;

	@Column(name = "settlementRole", nullable = false)
	@Enumerated(STRING)
	private SettlementRole settlementRole;

	@ManyToOne(fetch = FetchType.LAZY)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	private Settlement settlement;

	public SettlementUser(SettlementRole settlementRole, User user, Settlement settlement) {
		this.settlementRole = settlementRole;
		this.user = user;
		this.settlement = settlement;
	}
}
