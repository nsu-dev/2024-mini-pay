package org.c4marathon.assignment.user.domain;

import static jakarta.persistence.EnumType.*;
import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

import java.util.ArrayList;
import java.util.List;

import org.c4marathon.assignment.account.domain.Account;
import org.c4marathon.assignment.common.domain.BaseTimeEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
@Table(name = "users")
public class User extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "users_id")
	private Long id;

	@Column(name = "email", nullable = false, unique = true)
	private String email;

	@Column(name = "password", nullable = false)
	private String password;

	@Column(name = "name", nullable = false)
	private String name;

	@Enumerated(STRING)
	@Column(name = "role", nullable = false)
	private UserRole role;

	@OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
	private List<Account> accountList = new ArrayList<>();

	@Builder
	private User(String email, String password, String name, UserRole role) {
		this.email = email;
		this.password = password;
		this.name = name;
		this.role = role;
	}
}
