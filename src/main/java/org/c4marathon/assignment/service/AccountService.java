package org.c4marathon.assignment.service;

import java.time.LocalDate;

import org.c4marathon.assignment.Exception.InsufficientBalanceException;
import org.c4marathon.assignment.domain.Account;
import org.c4marathon.assignment.domain.AccountType;
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
	@Transactional
	public boolean addSavingsAccount(Long userId, AccountType type, int balance) {
		User user = findUserById(userId);
		// 적금 계좌 생성 및 저장
		Account savingsAccount = createAndSaveAccount(user, type, balance);

		// 적금 계좌를 User 객체에 추가
		user.addSavingAccount(savingsAccount);
		// 적금 계좌가 User 엔터티에 반영되도록 저장
		userRepository.save(user);
		return true;
	}

	// 메인 계좌에서 적금 계좌로 송금
	@Transactional
	public boolean transferToSavings(Long userId, Long savingsAccountId, int money) {
		User user = findUserById(userId);
		Account mainAccount = user.getMainAccount();
		Account savingsAccount = findAccountById(savingsAccountId);

		if (mainAccount.getBalance() < money) {
			throw new InsufficientBalanceException("잔액이 부족합니다.");
		}

		// 송금 처리
		executeTransfer(mainAccount, savingsAccount, money);

		// 변경된 계좌 정보 저장
		accountRepository.save(mainAccount);
		accountRepository.save(savingsAccount);

		return true;
	}

	//외부 계좌에서 돈을 가져오는 메서드(입금)
	@Transactional(isolation = Isolation.REPEATABLE_READ)
	public boolean transferFromExternalAccount(Long userId, Long externalUserId, int money) {
		// 사용자와 외부 사용자 찾기
		User user = findUserById(userId);
		User externalUser = findUserById(externalUserId);

		Account userMainAccount = user.getMainAccount();
		Account externalMainAccount = externalUser.getMainAccount();

		// 메인 계좌 여부 확인
		checkMainAccount(userMainAccount, externalMainAccount);
		// 한도 확인
		checkTransferLimit(userMainAccount, money);

		// 송금 처리 (외부 계좌에서 사용자 메인 계좌로)
		externalMainAccount.withdraw(money, LocalDate.now());  // 외부 계좌에서 출금
		userMainAccount.deposit(money);  // 사용자 계좌에 입금

		// 외부 계좌와 사용자 계좌 저장
		accountRepository.save(externalMainAccount);  // 외부 계좌 저장
		accountRepository.save(userMainAccount);      // 사용자 계좌 저장

		return true;
	}

	// 외부 메인 계좌로 송금
	@Transactional(isolation = Isolation.REPEATABLE_READ)
	public boolean transferToExternalMainAccount(Long userId, Long externalUserId, int money) {
		// 사용자와 외부 사용자 찾기
		User user = findUserById(userId);
		User externalUser = findUserById(externalUserId);

		Account userMainAccount = user.getMainAccount();
		Account externalMainAccount = externalUser.getMainAccount();

		// 메인 계좌 여부 확인
		checkMainAccount(userMainAccount, externalMainAccount);

		// 부족한 금액이 있을 경우 자동 충전 처리
		if (userMainAccount.getBalance() < money) {
			handleAutoCharge(userMainAccount, money - userMainAccount.getBalance());
		}

		// 송금 처리 (사용자 메인 계좌에서 외부 계좌로 송금)
		userMainAccount.withdraw(money, LocalDate.now());  // 사용자 메인 계좌에서 출금
		externalMainAccount.deposit(money);  // 외부 계좌에 입금

		// 사용자 계좌와 외부 계좌 저장
		accountRepository.save(userMainAccount);  // 사용자 메인 계좌 저장
		accountRepository.save(externalMainAccount);  // 외부 계좌 저장

		return true;
	}

	//메서드 역할 분리
	// 사용자 ID로 User 찾기
	public User findUserById(Long userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
	}

	// 계좌 ID로 Account 찾기
	public Account findAccountById(Long accountId) {
		return accountRepository.findById(accountId)
			.orElseThrow(() -> new IllegalArgumentException("계좌를 찾을 수 없습니다."));
	}

	// 메인 계좌 여부 확인
	public void checkMainAccount(Account... accounts) {
		for (Account account : accounts) {
			if (!account.isMainAccount()) {
				throw new IllegalArgumentException("모든 계좌는 메인 계좌여야 합니다.");
			}
		}
	}

	// 충전 한도 확인
	public void checkTransferLimit(Account account, int money) {
		if (account.getTodayChargeMoney() + money > account.getDailyChargeLimit()) {
			throw new IllegalArgumentException("오늘의 충전 한도를 초과했습니다.");
		}
	}

	// 송금 처리
	public void executeTransfer(Account fromAccount, Account toAccount, int money) {
		LocalDate today = LocalDate.now();
		fromAccount.withdraw(money, today);
		toAccount.deposit(money);  // 적금 계좌에 돈을 입금
	}

	// 적금 계좌 생성 및 저장
	public Account createAndSaveAccount(User user, AccountType type, int balance) {
		Account account = new Account(type, balance, user);
		return accountRepository.save(account);
	}

	// 자동 충전 처리
	public void handleAutoCharge(Account userMainAccount, int money) {
		LocalDate today = LocalDate.now();
		int remainingMoney = money - userMainAccount.getBalance(); // 부족 금액 계산

		while (remainingMoney > 0) {
			int chargeMoney = Math.min(10000, remainingMoney); // 10,000원 단위로 충전
			checkTransferLimit(userMainAccount, chargeMoney);   // 충전 한도 체크

			// 충전 수행
			userMainAccount.deposit(chargeMoney);
			userMainAccount.addTodayChargeMoney(chargeMoney);

			remainingMoney -= chargeMoney; // 남은 금액에서 충전 금액을 차감
		}
	}
}
