package org.c4marathon.assignment.domain.settlement.repository;

import java.util.List;
import java.util.Optional;

import org.c4marathon.assignment.domain.settlement.entity.settlement.Settlement;
import org.c4marathon.assignment.domain.settlement.entity.settlement.SettlementRole;
import org.c4marathon.assignment.domain.settlement.entity.settlement.SettlementUser;
import org.c4marathon.assignment.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SettlementUserRepository extends JpaRepository<SettlementUser, Long> {
	Optional<List<SettlementUser>> findAllByUser(User user);

	@Query("select user from SettlementUser where settlementRole = :settlementRole and settlement = :settlement")
	User findReceiver(
		@Param("settlementRole") SettlementRole settlementRole,
		@Param("settlement") Settlement settlement
	);

	@Query("select count(*) from SettlementUser where settlement = :settlement")
	int countRemainingUsers(@Param("settlement") Settlement settlement);
}
