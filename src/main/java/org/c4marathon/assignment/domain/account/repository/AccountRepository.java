package org.c4marathon.assignment.domain.account.repository;

import org.c4marathon.assignment.domain.account.entity.account.Account;
import org.c4marathon.assignment.domain.account.entity.account.AccountRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountRepository extends JpaRepository<Account, Long> {
	boolean existsByAccountNum(Long accountNum);

	Account findByAccountNum(Long accountNum);

	@Query("SELECT a FROM Account a WHERE a.user.userId = :userId and a.accountRole = :accountRole")
	Account findMainAccount(@Param("userId") Long userId, @Param("accountRole") AccountRole accountRole);
}
