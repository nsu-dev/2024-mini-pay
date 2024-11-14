package org.c4marathon.assignment.account.repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.c4marathon.assignment.account.domain.Account;
import org.c4marathon.assignment.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findAllByUser(User user);

    @Query("SELECT a FROM Account a JOIN FETCH a.user WHERE a.id = :id")
    Optional<Account> findById(@Param("id") Long accountId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a JOIN FETCH a.user WHERE a.id = :id")
    Optional<Account> findByIdWithLock(@Param("id") Long accountId);
}
