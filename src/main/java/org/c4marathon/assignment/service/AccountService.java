package org.c4marathon.assignment.service;

import java.time.LocalDate;

import org.c4marathon.assignment.domain.Account;
import org.c4marathon.assignment.domain.User;
import org.c4marathon.assignment.repository.AccountRepository;
import org.c4marathon.assignment.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.REPEATABLE_READ)
public class AccountService {

	private final AccountRepository accountRepository;
	private final UserRepository userRepository;

	// 적금 계좌 추가
	public boolean addSavingsAccount(Long userId, String type, int balance) {
		try {
			User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

			// 적금 계좌 생성 및 저장
			Account savingsAccount = accountRepository.save(new Account(type, balance, user));

			// 적금 계좌를 User 객체에 추가
			user.addSavingAccount(savingsAccount);

			// 적금 계좌가 User 엔터티에 반영되도록 저장
			userRepository.save(user);
			return true;
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("적금 계좌 추가 중 오류가 발생했습니다: " + e.getMessage());
		}
	}

	// 메인 계좌에서 적금 계좌로 송금
	@Transactional(isolation = Isolation.REPEATABLE_READ)
	public boolean transferToSavings(Long userId, Long savingsAccountId, int money) {
		try {
			User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

			Account mainAccount = user.getMainAccount();
			Account savingsAccount = accountRepository.findById(savingsAccountId)
				.orElseThrow(() -> new IllegalArgumentException("적금 계좌를 찾을 수 없습니다."));

			// 메인 계좌에서 출금
			mainAccount.withdraw(money, LocalDate.now());

			// 적금 계좌로 입금
			savingsAccount.deposit(money);

			// 더티 체킹에 의해 자동 저장되므로 따로 save()를 호출할 필요 없음
			// accountRepository.save(mainAccount);
			// accountRepository.save(savingsAccount);
			return true;
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("송금 중 오류가 발생했습니다: " + e.getMessage());
		} catch (Exception e) {
			throw new RuntimeException("알 수 없는 오류가 발생했습니다.");
		}
	}
}