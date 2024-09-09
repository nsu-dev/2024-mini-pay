package org.c4marathon.assignment.account.repository;

import java.util.List;
import java.util.Optional;

import org.c4marathon.assignment.account.domain.Account;
import org.c4marathon.assignment.account.domain.AccountType;
import org.c4marathon.assignment.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

	List<Account> findAllByUser(User user);

	@Query("SELECT a FROM Account a JOIN FETCH a.user WHERE a.id = :id AND a.type = :type")
	Optional<Account> findByIdAndType(@Param("id") Long accountId, @Param("type") AccountType findAccountType);
}
