package org.c4marathon.assignment.domain.account.entity.settlement;

import org.c4marathon.assignment.domain.user.entity.User;

import jakarta.persistence.Entity;
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

	@ManyToOne(fetch = FetchType.LAZY)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	private Settlement settlement;
}
