package org.c4marathon.assignment.domain.account.repository;

import org.c4marathon.assignment.domain.account.entity.settlement.Settlement_User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementUserRepository extends JpaRepository<Settlement_User, Long> {
}
