package org.c4marathon.assignment;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.c4marathon.assignment.domain.Account;
import org.c4marathon.assignment.domain.AccountType;
import org.c4marathon.assignment.domain.User;
import org.c4marathon.assignment.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class DomainTest {

	private Account mainAccount;
	private Account savingsAccount;
	private AccountService accountService;  // 직접 객체 생성
	private User user;

	@BeforeEach
	public void setUp() {
		// 기본 유저 생성
		user = new User();

		// 메인 계좌와 적금 계좌 생성
		mainAccount = new Account(AccountType.MAIN, 1_000_000, user);
		savingsAccount = new Account(AccountType.SAVINGS, 200_000, user);
	}

	@Test
	@DisplayName("잔액 출금 성공 테스트")
	public void withdrawSuccessTest() {
		// Given - 50만원 출금 요청
		LocalDate today = LocalDate.now();

		// When
		mainAccount.withdraw(500_000, today);

		// Then
		assertEquals(500_000, mainAccount.getBalance()); // 잔액이 50만원으로 감소
		assertEquals(500_000, mainAccount.getTodayChargeMoney()); // 당일 충전 금액이 50만원 증가
	}

	@Test
	@DisplayName("잔액 출금 실패 테스트 - 잔액 부족")
	public void withdrawInsufficientBalanceTest() {
		// Given - 잔액보다 많은 금액을 출금하려고 시도
		LocalDate today = LocalDate.now();

		// When & Then
		assertThrows(IllegalArgumentException.class, () -> {
			mainAccount.withdraw(1_500_000, today); // 100만원이 넘는 금액 출금 시도
		});

		// 잔액은 그대로 유지됨
		assertEquals(1_000_000, mainAccount.getBalance());
	}

	@Test
	@DisplayName("출금 실패 테스트 - 일일 한도 초과")
	public void withdrawOverDailyLimitTest() {
		// Given - 일일 출금 한도를 초과하는 금액 출금 시도
		LocalDate today = LocalDate.now();

		// When & Then
		assertThrows(IllegalArgumentException.class, () -> {
			mainAccount.withdraw(3_500_000, today); // 300만원 한도 초과 시도
		});

		// 잔액은 그대로 유지됨
		assertEquals(1_000_000, mainAccount.getBalance());
	}

	@Test
	@DisplayName("입금 성공 테스트")
	public void depositSuccessTest() {
		// Given - 30만원 입금 요청
		int depositAmount = 300_000;

		// When
		mainAccount.deposit(depositAmount);

		// Then
		assertEquals(1_300_000, mainAccount.getBalance()); // 잔액이 130만원으로 증가
	}

	@Test
	@DisplayName("메인 계좌 확인 테스트")
	public void isMainAccountTest() {
		// When & Then
		assertTrue(mainAccount.isMainAccount()); // 메인 계좌는 true
		assertFalse(savingsAccount.isMainAccount()); // 적금 계좌는 false
	}

	@Test
	@DisplayName("출금 날짜 변경에 따른 일일 충전 금액 초기화 테스트")
	public void withdrawDateChangeResetsDailyChargeTest() {
		// Given - 첫 번째 출금: 오늘 출금
		LocalDate today = LocalDate.now();
		mainAccount.withdraw(500_000, today);

		assertEquals(500_000, mainAccount.getTodayChargeMoney());

		// 두 번째 출금: 다음 날
		LocalDate tomorrow = today.plusDays(1);
		mainAccount.withdraw(300_000, tomorrow);

		// Then
		assertEquals(300_000, mainAccount.getTodayChargeMoney()); // 다음 날 충전 금액 초기화됨
		assertEquals(200_000, mainAccount.getBalance()); // 잔액이 적절히 감소됨
	}

}