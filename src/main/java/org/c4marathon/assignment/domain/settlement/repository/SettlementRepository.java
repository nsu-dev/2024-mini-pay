package org.c4marathon.assignment.domain.settlement.repository;

import org.c4marathon.assignment.domain.settlement.entity.settlement.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
}
