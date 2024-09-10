package org.c4marathon.assignment.domain.account.entity;

import java.time.LocalDateTime;

import org.c4marathon.assignment.domain.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "account")
public class Account {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long accountId;
	@Column(name = "accountNum", unique = true, nullable = false)
	private String accountNum;
	@Enumerated(EnumType.STRING)
	@Column(name = "role", nullable = false)
	private AccountRole accountRole;
	@Column(name = "registeredAt", nullable = false)
	private LocalDateTime registeredAt;
	@Column(name = "accountBalance", nullable = false)
	private Long accountBalance;
	@Enumerated(EnumType.STRING)
	@Column(name = "accountStatus", nullable = false)
	private AccountStatus accountStatus;
	@Column(name = "dailyChargeLimit", nullable = false)
	private int dailyChargeLimit;

	@ManyToOne(fetch = FetchType.LAZY)
	private User user;

	@Builder
	private Account(String accountNum, AccountRole accountRole, LocalDateTime registeredAt, Long accountBalance,
		AccountStatus accountStatus, int dailyChargeLimit, User user) {
		this.accountNum = accountNum;
		this.accountRole = accountRole;
		this.registeredAt = registeredAt;
		this.accountBalance = accountBalance;
		this.accountStatus = accountStatus;
		this.dailyChargeLimit = dailyChargeLimit;
		this.user = user;
	}

	public void updateAccount(Long accountBalance, int dailyChargeLimit) {
		this.accountBalance = accountBalance;
		this.dailyChargeLimit = dailyChargeLimit;
	}

	public void updateSaving(Long accountBalance) {
		this.accountBalance = accountBalance;
	}
}
