package org.c4marathon.assignment.Dto;

public class UserDto {
    private Long userId;
    private String password;
    private String name;
    private String registrationNum;
    private String mainAccount;
    private String savingAccount;

    public UserDto(Long userId, String password, String name, String registrationNum, String mainAccount, String savingAccount){
        this.userId = userId;
        this.password = password;
        this.name = name;
        this.registrationNum = registrationNum;
        this.mainAccount = mainAccount;
        this.savingAccount = savingAccount;
    }

    //빌더만들기
}
