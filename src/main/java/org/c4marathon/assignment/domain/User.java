package org.c4marathon.assignment.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
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

    //기본 생성자
    public User() {
        this.savingAccounts = new ArrayList<>();
    }

    //메인 계좌 설정 메서드
    public void setMainAccount(Account mainAccount) {
        this.mainAccount = mainAccount;
    }

    //적금 계좌 추가: Account 생성 시 User를 전달
    public void addSavingAccount(String type, int balance) {
        Account savingAccount = new Account(type, balance, this);  // 생성자에서 User 설정
        this.savingAccounts.add(savingAccount); // savingAccounts 리스트에 적금 계좌 추가
    }
}
