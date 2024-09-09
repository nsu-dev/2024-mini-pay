package org.c4marathon.assignment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long accountId;
    private double balance;
    //일반계좌인지 적금계좌인지
    private String type;
    private double dailyWithdrawalLimit;

    //다대일 관계(여러 계좌가 하나의 유저에 속함)
    @ManyToOne
    @JoinColumn(name = "userId")
    private User user;

    public Account() {}

    public Account(String type, double initialBalance){
        this.type=type;
        this.balance=initialBalance;
        this.dailyWithdrawalLimit= 3000000; // 1일 출금 한도 3백만원
    }

}
