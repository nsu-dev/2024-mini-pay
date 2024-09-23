package org.c4marathon.assignment.user.domain;

import java.util.ArrayList;
import java.util.List;

import org.c4marathon.assignment.account.domain.Account;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Table(name = "users")
@Getter
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "users_id")
	private Long id;

	@Column(name = "userId", nullable = false, unique = true)
	private String userId;

	@Column(name = "userPw", nullable = false)
	private String userPw;

	@Column(name = "name", nullable = false)
	private String name;

	@OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
	private List<Account> accountList = new ArrayList<>();

	@Column(name = "accountPw", nullable = false)
	private int accountPw;

	@Builder
	public User(String userId, String userPw, String name, int accountPw) {
		this.userId = userId;
		this.userPw = userPw;
		this.name = name;
		this.accountPw = accountPw;
	}
}
