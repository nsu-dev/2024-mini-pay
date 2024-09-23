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
public class AccountService {

	private final AccountRepository accountRepository;
	private final UserRepository userRepository;

	// 적금 계좌 추가
	@Transactional(isolation = Isolation.REPEATABLE_READ)
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
			return true;
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("송금 중 오류가 발생했습니다: " + e.getMessage());
		} catch (Exception e) {
			throw new RuntimeException("알 수 없는 오류가 발생했습니다.");
		}
	}

	//외부 계좌에서 돈을 가져오는 메서드
	@Transactional(isolation = Isolation.REPEATABLE_READ)
	public boolean transferFromExternalAccount(Long userId, Long externalUserId, int money) {
		try {
			//사용자와 외부 사용자 찾기
			User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

			User externalUser = userRepository.findById(externalUserId)
				.orElseThrow(() -> new IllegalArgumentException("외부 사용자를 찾을 수 없습니다."));

			Account userMainAccount = user.getMainAccount();
			Account externalMainAccount = externalUser.getMainAccount();

			// 두 계좌 모두 메인 계좌인지 확인
			if (!userMainAccount.isMainAccount() || !externalMainAccount.isMainAccount()) {
				throw new IllegalArgumentException("두 계좌 모두 메인 계좌여야 합니다.");
			}

			//오늘 날짜를 가져오기
			LocalDate today = LocalDate.now();

			// 충전 한도 체크
			if (userMainAccount.getTodayChargeAmount() + money > userMainAccount.getDailyChargeLimit()) {
				throw new IllegalArgumentException("오늘의 충전 한도를 초과했습니다.");
			}

			// 외부 계좌의 잔액 확인
			if (externalMainAccount.getBalance() < money) {
				throw new IllegalArgumentException("외부 계좌의 잔액이 부족합니다.");
			}

			// 외부 계좌에서 돈을 차감하고 사용자 계좌에 돈을 입금
			externalMainAccount.withdraw(money, today); // 외부 계좌에서 출금
			userMainAccount.deposit(money); // 사용자 계좌에 입금

			// 오늘의 충전 금액 업데이트
			userMainAccount.addTodayChargeAmount(money);

			return true;
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("입금 중 오류가 발생했습니다: " + e.getMessage());
		} catch (Exception e) {
			throw new RuntimeException("알 수 없는 오류가 발생했습니다.");
		}
	}
}