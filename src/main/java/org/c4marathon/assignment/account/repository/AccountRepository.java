package org.c4marathon.assignment.account.repository;

import java.util.List;
import java.util.Optional;

import org.c4marathon.assignment.account.domain.Account;
import org.c4marathon.assignment.account.domain.AccountType;
import org.c4marathon.assignment.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

	List<Account> findAllByUser(User user);

	Optional<Account> findByIdAndType(Long accountId, AccountType findAccountType);
}
