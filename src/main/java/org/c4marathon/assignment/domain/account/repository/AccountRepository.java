package org.c4marathon.assignment.domain.account.repository;

import org.c4marathon.assignment.domain.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;

public interface AccountRepository extends JpaRepository<Account, Long> {
	boolean existsByAccountNum(String accountNum);

	@Lock(LockModeType.PESSIMISTIC_READ)
	Account findByAccountNum(String accountNum);
}
