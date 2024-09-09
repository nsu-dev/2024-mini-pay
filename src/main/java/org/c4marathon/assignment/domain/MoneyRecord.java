package org.c4marathon.assignment.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
public class MoneyRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double amount;
    private LocalDate date; //출금 날짜

    @ManyToOne
    @JoinColumn(name="accountId")
    private Account account; //출금한 계좌

}
