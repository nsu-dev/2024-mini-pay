package org.c4marathon.assignment.domain.user;

import org.c4marathon.assignment.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}