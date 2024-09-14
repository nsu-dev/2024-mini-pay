package org.c4marathon.assignment.domain;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor //기본생성자 자동 추가
public class Account {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long accountId;
	private int balance;

	//계좌 유형을 추가하여 메인 계쫘와 적금 계좌를 구분
	private String type;
	private int dailyWithdrawalLimit = 3000000; // 1일 출금 한도 3백만원

	private int todayWithdrawnAmount = 0; // 당일 출금한 금액
	private LocalDate lastWithdrawalDate; // 마지막 출금 날짜

	// 다대일 관계 (적금 계좌가 하나의 유저에 속함)
	@ManyToOne
	@JoinColumn(name = "userId")
	private User user;

	//사용자와의 관계를 설정하는 생성자
	public Account(String type, int initialBalance, User user) {
		this.type = type;
		this.balance = initialBalance;
		this.lastWithdrawalDate = LocalDate.now(); // 계좌 생성 시 초기화
		this.user = user;
	}

	// 충전 로직
	public void withdraw(int money, LocalDate today) { //날짜 매개변수로 받아오기 -> 매개변수로 안하면 픽스돼서 테스트로 변경이 안됨

		// 출금 날짜가 달라지면 당일 출금 금액을 초기화
		if (!today.equals(lastWithdrawalDate)) {
			todayWithdrawnAmount = 0;
			lastWithdrawalDate = today;
		}

		// 출금 한도 체크
		if (todayWithdrawnAmount + money > dailyWithdrawalLimit) {
			throw new IllegalArgumentException("오늘의 출금 한도를 초과했습니다.");
		}

		// 잔액 체크
		if (money > balance) {
			throw new IllegalArgumentException("잔액이 부족합니다.");
		}

		// 출금 처리
		balance -= money;
		todayWithdrawnAmount += money;

	}

	//입금 로직
	public void deposit(int money) {
		balance += money;
	}

	// 계좌 타입 반환
	public String getAccountType() {
		return type;
	}

	// 금액 반환
	public int getBalance() {
		return balance;
	}

	// 사용자 반환
	public User getUser() {
		return user;
	}
}