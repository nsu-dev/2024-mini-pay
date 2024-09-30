package org.c4marathon.assignment.domain.account.repository;

import org.c4marathon.assignment.domain.account.entity.settlement.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
}
