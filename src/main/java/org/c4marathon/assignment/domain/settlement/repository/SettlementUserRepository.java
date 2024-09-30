package org.c4marathon.assignment.domain.settlement.repository;

import java.util.List;
import java.util.Optional;

import org.c4marathon.assignment.domain.settlement.entity.settlement.SettlementUser;
import org.c4marathon.assignment.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementUserRepository extends JpaRepository<SettlementUser, Long> {
	Optional<List<SettlementUser>> findAllByUser(User user);
}
