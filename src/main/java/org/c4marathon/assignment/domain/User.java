package org.c4marathon.assignment.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
    private String password;
    private String name;
    private String registrationNum;

    //메인 계좌는 1대1 관계
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "mainAccountId")
    private Account mainAccount;

    //사용자와 적금계좌는 1대 다 관계
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    private List<Account> savingAccounts = new ArrayList<>();

    public User(){
        this.savingAccounts = new ArrayList<>();
    }

}
