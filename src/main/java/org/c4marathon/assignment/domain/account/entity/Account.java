package org.c4marathon.assignment.domain.account.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
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
}
