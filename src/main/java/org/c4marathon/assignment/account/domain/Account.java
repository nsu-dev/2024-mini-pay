package org.c4marathon.assignment.account.domain;

import org.c4marathon.assignment.account.exception.MainAccountException;
import org.c4marathon.assignment.common.exception.BaseException;
import org.c4marathon.assignment.user.domain.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Table(name = "accounts")
@Getter
public class Account {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "account_id")
	private Long id;

	@Column(name = "accountNumber", nullable = false, unique = true)
	private Long accountNum;

	@Enumerated(EnumType.STRING)
	@Column(name = "account_type", nullable = false)
	private AccountType type;

	@Column(name = "amount", nullable = false)
	private int amount;

	@Column(name = "accountPw", nullable = false)
	private int accountPw;

	@Column(name = "limitaccount", nullable = false)
	private int limitaccount;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "users_id")
	private User user;

	public void resetLimitAccount() {
		this.limitaccount = 3000000;
	}

	public void setAmount(int updatedAmount) {
		this.amount += updatedAmount;
		this.limitaccount -= updatedAmount;
		if (this.limitaccount < 0) {
			throw new BaseException(MainAccountException.LIMIT_ACCOUNT);
		}
	}

	public void increaseAmount(int updatedAmount) {
		this.amount += updatedAmount;
	}

	public void reduceAmount(int updatedAmount) {
		this.amount -= updatedAmount;
	}

	@Builder
	public Account(Long accountNum, AccountType type, int amount, int accountPw, int limitaccount, User user) {
		this.accountNum = accountNum;
		this.type = type;
		this.amount = amount;
		this.accountPw = accountPw;
		this.limitaccount = limitaccount;
		this.user = user;
	}
}
