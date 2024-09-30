package org.c4marathon.assignment.domain.user.entity;

import java.util.ArrayList;
import java.util.List;

import org.c4marathon.assignment.domain.account.entity.account.Account;
import org.c4marathon.assignment.domain.account.entity.settlement.Settlement_User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

	@OneToMany(mappedBy = "user")
	private List<Settlement_User> settlementUserList = new ArrayList<>();

	@Builder
	private User(String userPhone, String userPassword, String userName, String userBirth) {
		this.userPhone = userPhone;
		this.userPassword = userPassword;
		this.userName = userName;
		this.userBirth = userBirth;
	}
}