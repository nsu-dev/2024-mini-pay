package org.c4marathon.assignment.account.repository;

import java.util.Optional;

import org.c4marathon.assignment.account.domain.Account;
import org.c4marathon.assignment.account.domain.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
	@Query("SELECT a FROM Account a WHERE a.accountNum = :accountNum")
	Optional<Account> findByAccount(Long accountNum);

	@Query("SELECT a FROM Account a WHERE a.user.userId = :userId AND a.type = :type")
	Optional<Account> findByMainAccount(@Param("userId") String userId, @Param("type") AccountType type);

}
