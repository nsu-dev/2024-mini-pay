package org.c4marathon.assignment.service;

import java.time.LocalDate;

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
	@Transactional(isolation = Isolation.REPEATABLE_READ)
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
	@Transactional(isolation = Isolation.REPEATABLE_READ)
	public boolean transferToSavings(Long userId, Long savingsAccountId, int money) {
		User user = findUserById(userId);
		Account mainAccount = user.getMainAccount();
		Account savingsAccount = findAccountById(savingsAccountId);

		executeTransfer(mainAccount, savingsAccount, money);
		return true;
	}

	//외부 계좌에서 돈을 가져오는 메서드
	@Transactional(isolation = Isolation.REPEATABLE_READ)
	public boolean transferFromExternalAccount(Long userId, Long externalUserId, int money) {
		//사용자와 외부 사용자 찾기
		User user = findUserById(userId);
		User externalUser = findUserById(userId);

		Account userMainAccount = user.getMainAccount();
		Account externalMainAccount = externalUser.getMainAccount();

		checkMainAccount(userMainAccount, externalMainAccount);
		checkTransferLimit(userMainAccount, money);
		executeTransfer(externalMainAccount, userMainAccount, money);

		return true;
	}

	//메서드 역할 분리
	// 사용자 ID로 User 찾기
	private User findUserById(Long userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
	}

	// 계좌 ID로 Account 찾기
	private Account findAccountById(Long accountId) {
		return accountRepository.findById(accountId)
			.orElseThrow(() -> new IllegalArgumentException("계좌를 찾을 수 없습니다."));
	}

	// 메인 계좌 여부 확인
	private void checkMainAccount(Account... accounts) {
		for (Account account : accounts) {
			if (!account.isMainAccount()) {
				throw new IllegalArgumentException("모든 계좌는 메인 계좌여야 합니다.");
			}
		}
	}

	// 충전 한도 확인
	private void checkTransferLimit(Account account, int money) {
		if (account.getTodayChargeMoney() + money > account.getDailyChargeLimit()) {
			throw new IllegalArgumentException("오늘의 충전 한도를 초과했습니다.");
		}
	}

	// 송금 처리
	private void executeTransfer(Account fromAccount, Account toAccount, int money) {
		LocalDate today = LocalDate.now();
		fromAccount.withdraw(money, today);
		toAccount.deposit(money);
		toAccount.addTodayChargeMoney(money);
	}

	// 적금 계좌 생성 및 저장
	private Account createAndSaveAccount(User user, AccountType type, int balance) {
		Account account = new Account(type, balance, user);
		return accountRepository.save(account);
	}
}