package org.c4marathon.assignment.domain.account.repository;

import org.c4marathon.assignment.domain.account.entity.settlement.SettlementUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementUserRepository extends JpaRepository<SettlementUser, Long> {
}
