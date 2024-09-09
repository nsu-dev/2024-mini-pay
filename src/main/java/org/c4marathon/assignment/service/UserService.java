package org.c4marathon.assignment.service;
import org.c4marathon.assignment.domain.Account;
import org.c4marathon.assignment.domain.User;
import org.c4marathon.assignment.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private org.c4marathon.assignment.repository.AccountRepository accountRepository;

    //사용자 회원가입(메인계좌 생성)
    @Transactional
    public User reigisterUser(String name, String password, String registrationNum) {
        User user = new User();
        user.setName(name);
        user.setPassword(password);
        user.setRegistrationNum(registrationNum);

        //메인 계좌 자동 생성
        Account mainAccount = new Account("Main Account", 0.0);
        user.setMainAccount(mainAccount);

        //사용자와 계좌 저장
        return userRepository.save(user);
    }

    @Transactional
    public void assSavingsAccount(Long userId){
        User user = userRepository.findById(userId).orElseThrow(()-> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        //적금 계좌 생성
        Account savingsAccount = new Account("Savings Account", 0.0);
        savingsAccount.setUser(user);

        //적금 계좌 저장
        accountRepository.save(savingsAccount);
    }

    // 메인 계좌에서 적금 계좌로 송금 (트랜잭션 관리)
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public boolean transferToSavings(Long userId, Long savingsAccountId, double amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Account mainAccount = user.getMainAccount();
        Account savingsAccount = accountRepository.findById(savingsAccountId)
                .orElseThrow(() -> new IllegalArgumentException("적금 계좌를 찾을 수 없습니다."));

        // 출금 한도 체크
        if (amount > mainAccount.getDailyWithdrawalLimit()) {
            throw new IllegalArgumentException("1일 출금한도 초과입니다.");
        }

        // 메인 계좌 잔액 체크
        if (amount > mainAccount.getBalance()) {
            throw new IllegalArgumentException("메인 계좌 금액이 부족합니다.");
        }

        // 메인 계좌에서 출금 및 적금 계좌로 입금
        mainAccount.setBalance(mainAccount.getBalance() - amount);
        savingsAccount.setBalance(savingsAccount.getBalance() + amount);

        // 계좌 저장
        accountRepository.save(mainAccount);
        accountRepository.save(savingsAccount);

        return true;
    }

}
