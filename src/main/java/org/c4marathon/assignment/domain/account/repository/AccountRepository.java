package org.c4marathon.assignment.domain.account.repository;

import org.c4marathon.assignment.domain.account.entity.Account;
import org.c4marathon.assignment.domain.account.entity.AccountRole;
import org.c4marathon.assignment.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface AccountRepository extends JpaRepository<Account, Long> {
	boolean existsByAccountNum(Long accountNum);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Account findByAccountNum(Long accountNum);

	@Query("SELECT a FROM Account a WHERE a.user.userId = :userId and a.accountRole = :accountRole")
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Account findMainAccount(@Param("userId") Long userId, @Param("accountRole") AccountRole accountRole);

	@Query("select a.user from Account a where a.accountId = :accountId")
	User findUserByAccount(@Param("accountId") Long accountId);

}
