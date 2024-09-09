package org.c4marathon.assignment.Dto;


public class AccountDto {
    private Long accountId;
    private double balance;
    //일반계좌인지 적금계좌인지
    private String type;
    private double dailyWithdrawalLimit;

    public AccountDto(Long accountId, double balance, String type, double dailyWithdrawalLimit){
        this.accountId = accountId;
        this.balance = balance;
        this.type = type;
        this.dailyWithdrawalLimit=dailyWithdrawalLimit;
    }

    //빌더추가
}
