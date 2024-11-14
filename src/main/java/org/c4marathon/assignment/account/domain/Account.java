package org.c4marathon.assignment.account.domain;

import static jakarta.persistence.EnumType.*;
import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

import java.time.LocalDate;
import java.time.ZoneId;

import org.c4marathon.assignment.account.exception.AccountErrorCode;
import org.c4marathon.assignment.common.exception.runtime.BaseException;
import org.c4marathon.assignment.user.domain.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
@Table(name = "account")
public class Account {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "account_id")
	private Long id;

	@Enumerated(STRING)
	@Column(name = "account_type", nullable = false)
	private AccountType type;

	@Column(name = "amount", nullable = false)
	private int amount;

	@Column(name = "limit_amount", nullable = false)
	private int limitAmount;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "users_id")
	private User user;

	@Column(name = "last_charge_date")
	private LocalDate lastChargeDate;

	@Builder
	private Account(AccountType type, int amount, int limitAmount, User user) {
		this.type = type;
		this.amount = amount;
		this.limitAmount = limitAmount;
		this.user = user;
	}

	public int decreaseAmount(int amount) {
		if (this.amount < amount) {
			throw new BaseException(AccountErrorCode.NOT_ENOUGH_AMOUNT);
		}
		this.amount -= amount;

		return amount;
	}

	public void increaseAmount(int amount) {
		this.amount += amount;
	}

	public void chargeAmount(int chargeAmount) {
		if (this.limitAmount < chargeAmount) {
			throw new BaseException(AccountErrorCode.NOT_ENOUGH_CHARGE_AMOUNT);
		}
		this.amount += chargeAmount;
		this.limitAmount -= chargeAmount;
		this.lastChargeDate = LocalDate.now(ZoneId.of("Asia/Seoul"));
	}

	public void resetLimitAmount() {
		this.limitAmount = 3_000_000;
		this.lastChargeDate = LocalDate.now(ZoneId.of("Asia/Seoul"));
	}
}
