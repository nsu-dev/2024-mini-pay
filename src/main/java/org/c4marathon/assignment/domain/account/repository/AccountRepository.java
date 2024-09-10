package org.c4marathon.assignment.domain.account.repository;

import org.c4marathon.assignment.domain.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
	boolean existsByAccountNum(String accountNum);
}
