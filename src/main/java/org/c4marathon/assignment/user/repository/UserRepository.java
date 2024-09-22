package org.c4marathon.assignment.user.repository;

import java.util.Optional;

import org.c4marathon.assignment.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	boolean existsByEmail(String email);

	Optional<User> findByEmail(String Email);
}
