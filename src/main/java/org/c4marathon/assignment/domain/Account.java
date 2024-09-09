package org.c4marathon.assignment.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long accountId;
    private double balance;
    // 일반계좌인지 적금계좌인지
    private String type;
    private double dailyWithdrawalLimit = 3000000; // 1일 출금 한도 3백만원

    private double todayWithdrawnAmount = 0; // 당일 출금한 금액
    private LocalDate lastWithdrawalDate; // 마지막 출금 날짜

    // 다대일 관계 (여러 계좌가 하나의 유저에 속함)
    @ManyToOne
    @JoinColumn(name = "userId")
    private User user;

    public Account() {}

    public Account(String type, double initialBalance) {
        this.type = type;
        this.balance = initialBalance;
        this.lastWithdrawalDate = LocalDate.now(); // 계좌 생성 시 초기화
    }

    // 출금 로직
    public void withdraw(double amount) {
        LocalDate today = LocalDate.now();

        // 출금 날짜가 달라지면 당일 출금 금액을 초기화
        if (!today.equals(lastWithdrawalDate)) {
            todayWithdrawnAmount = 0;
            lastWithdrawalDate = today;
        }

        // 출금 한도 체크
        if (todayWithdrawnAmount + amount > dailyWithdrawalLimit) {
            throw new IllegalArgumentException("오늘의 출금 한도를 초과했습니다.");
        }

        // 잔액 체크
        if (amount > balance) {
            throw new IllegalArgumentException("잔액이 부족합니다.");
        }

        // 출금 처리
        balance -= amount;
        todayWithdrawnAmount += amount;
    }
}
