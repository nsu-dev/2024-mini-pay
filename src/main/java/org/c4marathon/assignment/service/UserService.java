package org.c4marathon.assignment.service;
import org.c4marathon.assignment.Dto.UserRequestDto;
import org.c4marathon.assignment.Dto.UserResponseDto;
import org.c4marathon.assignment.repository.AccountRepository;
import org.c4marathon.assignment.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AccountRepository accountRepository;

    //사용자 회원가입(메인계좌 생성)
    @Transactional
    public UserResponseDto registerUser(UserRequestDto userRequestDto) {
        User user = new User();
        // User 객체를 빌더 패턴으로 생성
        User user = new User.Builder()
                .password(userRequestDto.getPassword())
                .name(userRequestDto.getName())
                .registrationNum(userRequestDto.getRegistrationNum())
                .build();

        // 메인 계좌 설정
        Account mainAccount = new Account("Main Account", 0);
        user.setMainAccount(mainAccount);

        // 저장
        User savedUser = userRepository.save(user);

        // DTO 응답
        return new UserResponseDto.Builder()
                .userId(savedUser.getUserId())
                .name(savedUser.getName())
                .registrationNum(savedUser.getRegistrationNum())
                .build();
    }


    @Transactional
    public void addSavingsAccount(Long userId){
        User user = userRepository.findById(userId).orElseThrow(()-> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        //적금 계좌 생성
        Account savingsAccount = new Account("Savings Account", 0);
        savingsAccount.setUser(user);

        //적금 계좌 저장
        accountRepository.save(savingsAccount);
    }

    // 메인 계좌에서 적금 계좌로 송금 (트랜잭션 관리)
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public boolean transferToSavings(Long userId, Long savingsAccountId, int money) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Account mainAccount = user.getMainAccount();
        Account savingsAccount = accountRepository.findById(savingsAccountId)
                .orElseThrow(() -> new IllegalArgumentException("적금 계좌를 찾을 수 없습니다."));

        // 메인 계좌에서 출금
        mainAccount.withdraw(money);

        // 적금 계좌로 입금
        savingsAccount.setBalance(savingsAccount.getBalance() + money);

        // 계좌 저장
        accountRepository.save(mainAccount);
        accountRepository.save(savingsAccount);

        return true;
    }

}
