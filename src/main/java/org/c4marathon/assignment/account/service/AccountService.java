package org.c4marathon.assignment.account.service;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.c4marathon.assignment.account.domain.Account;
import org.c4marathon.assignment.account.domain.AccountType;
import org.c4marathon.assignment.account.dto.ChargeDto;
import org.c4marathon.assignment.account.dto.SavingAccountPwDto;
import org.c4marathon.assignment.account.dto.SendDto;
import org.c4marathon.assignment.account.exception.MainAccountException;
import org.c4marathon.assignment.account.exception.NotFountAccountException;
import org.c4marathon.assignment.account.repository.AccountRepository;
import org.c4marathon.assignment.common.exception.BaseException;
import org.c4marathon.assignment.user.domain.User;
import org.c4marathon.assignment.user.exception.LoginException;
import org.c4marathon.assignment.user.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {

	private final AccountRepository accountRepository;
	private final UserRepository userRepository;
	Long randomAccountNum = new Random().nextLong();

	// 정각마다 일일한도 초기화 서비스
	@Scheduled(cron = "0 0 0 * * *")
	@Transactional(isolation = Isolation.SERIALIZABLE)
	public void resetLimitAccount() {
		List<Account> accounts = accountRepository.findAll();
		for (Account account : accounts) {
			account.resetLimitAccount();
		}
	}

	// 메인 계좌 충전 서비스
	@Transactional(isolation = Isolation.REPEATABLE_READ)
	public boolean chargeMainAccount(ChargeDto chargeDto) {
		Optional<Account> optionalAccount = accountRepository.findByAccount(chargeDto.accountNum());
		optionalAccount.orElseThrow(() -> new BaseException(NotFountAccountException.NOT_FOUNT_ACCOUNT));
		Account account = optionalAccount.get();
		account.setAmount(chargeDto.chargeMoney());
		return true;
	}

	// 적금 계좌 생성 서비스
	@Transactional(isolation = Isolation.REPEATABLE_READ)
	public boolean craeteSavingAccount(String userId, SavingAccountPwDto savingAccountPwDto) {
		Optional<User> userOptional = userRepository.findByUserId(userId);
		userOptional.orElseThrow(()-> new BaseException(LoginException.NOT_FOUND_USER));

			User user = userOptional.get();
			Account account = Account.builder()
				.accountNum(randomAccountNum)
				.type(AccountType.SAVING_ACCOUNT)
				.accountPw(savingAccountPwDto.accountPw())
				.amount(0)
				.user(user)
				.build();

			accountRepository.save(account);
			return true;
	}

	// 메인 계좌 -> 적금 계좌로 송금 서비스
	@Transactional(isolation = Isolation.REPEATABLE_READ)
	public boolean sendSavingAccount(SendDto sendDto) {
		Optional<Account> optionalAccount = accountRepository.findByAccount(sendDto.accountNum());
		Optional<Account> optionalMyAccount = accountRepository.findByMyAccount(sendDto.accountPw());

		optionalAccount.orElseThrow(() -> new BaseException(NotFountAccountException.NOT_FOUNT_ACCOUNT));
		optionalMyAccount.orElseThrow(() -> new BaseException(NotFountAccountException.NOT_MATCH_ACCOUNT));

		Account main = optionalMyAccount.get();
		Account saving = optionalAccount.get();
		int checkMoney = main.getAmount() - sendDto.sendMoney();

		if (checkMoney > 0) {
			main.reduceAmount(sendDto.sendMoney());
			saving.increaseAmount(sendDto.sendMoney());
			accountRepository.save(main);
			accountRepository.save(saving);
			return true;
		} else {
			throw new BaseException(MainAccountException.SHORT_MONEY);
		}
	}

	// 메인 계좌 -> 다른 유저의 메인 계좌로 송금 서비스
	@Transactional(isolation = Isolation.REPEATABLE_READ)
	public boolean sendOtherAccount(SendDto sendDto) {
		Optional<Account> optionalMyAccount = accountRepository.findByMyAccount(sendDto.accountPw());
		Optional<Account> optionalAccount = accountRepository.findByAccount(sendDto.accountNum());

		optionalMyAccount.orElseThrow(() -> new BaseException(NotFountAccountException.NOT_MATCH_ACCOUNT));
		optionalAccount.orElseThrow(() -> new BaseException(NotFountAccountException.NOT_FOUNT_ACCOUNT));

		Account mainAccount = optionalMyAccount.get();
		Account otherAccount = optionalAccount.get();
		int checkMoney = mainAccount.getAmount() - sendDto.sendMoney();
		int lackingMoney = sendDto.sendMoney() - mainAccount.getAmount();

		if (checkMoney > 0) {
			otherAccount.increaseAmount(sendDto.sendMoney());
			mainAccount.reduceAmount(sendDto.sendMoney());
			accountRepository.save(mainAccount);
			accountRepository.save(otherAccount);
			return true;
		}else {
			int chargeMoney = (int)(Math.round(lackingMoney / 10000.0) * 10000);
			chargeMainAccount(new ChargeDto(mainAccount.getAccountNum(), chargeMoney));
		}
		return false;
	}
}
