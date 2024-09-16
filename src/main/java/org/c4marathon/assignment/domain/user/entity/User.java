package org.c4marathon.assignment.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.c4marathon.assignment.domain.account.entity.Account;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user")
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long userId;

	@Column(name = "userPhone", unique = true, nullable = false)
	private String userPhone;

	@Column(name = "userPassword", nullable = false)
	private String userPassword;

	@Column(name = "userName", nullable = false)
	private String userName;

	@Column(name = "userBirth", nullable = false)
	private String userBirth;

	@OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
	private List<Account> accountList = new ArrayList<>();

	@Builder
	private User(String userPhone, String userPassword, String userName, String userBirth) {
		this.userPhone = userPhone;
		this.userPassword = userPassword;
		this.userName = userName;
		this.userBirth = userBirth;
	}
}