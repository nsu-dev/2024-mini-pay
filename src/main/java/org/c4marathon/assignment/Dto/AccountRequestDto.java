package org.c4marathon.assignment.Dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class AccountRequestDto {
    private int balance;
    private String type;
    private int dailyWithdrawalLimit;

    @Builder
    public AccountRequestDto(int balance, String type, int dailyWithdrawalLimit){
        this.balance = balance;
        this.type = type;
        this.dailyWithdrawalLimit = dailyWithdrawalLimit;
    }

}
