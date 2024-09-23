package org.c4marathon.assignment.domain;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor // 기본 생성자 자동 추가
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long userId;
	private String password;
	private String name;
	private String registrationNum;

	//메인 계좌는 1대1 관계
	@OneToOne(cascade = CascadeType.REMOVE)
	@JoinColumn(name = "mainAccountId")
	private Account mainAccount;

	//사용자와 적금계좌는 1대 다 관계
	@OneToMany(cascade = CascadeType.REMOVE, mappedBy = "user")
	private List<Account> savingAccounts = new ArrayList<>();

	//@Builer를 통해 객체 생성 시 필드 값을 초기화
	@Builder
	public User(String password, String name, String registrationNum) {
		this.password = password;
		this.name = name;
		this.registrationNum = registrationNum;
		this.savingAccounts = new ArrayList<>();
	}

	// 테스트용 생성자: userId 포함!
	public User(Long userId, String password, String name, String registrationNum) {
		this.userId = userId;
		this.password = password;
		this.name = name;
		this.registrationNum = registrationNum;
		this.savingAccounts = new ArrayList<>();
	}

	//메인 계좌 설정 메서드
	public void setMainAccount(Account mainAccount) {
		this.mainAccount = mainAccount;
	}

	//적금 계좌 추가: Account 생성 시 User를 전달
	public void addSavingAccount(Account account) {
		this.savingAccounts.add(account);
	}

	public List<Account> getSavingAccounts() {
		return savingAccounts;
	}

}