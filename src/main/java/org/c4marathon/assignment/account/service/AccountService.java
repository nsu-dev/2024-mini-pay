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
import org.c4marathon.assignment.user.exception.NotFoundException;
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
	public void chargeMainAccount(ChargeDto chargeDto) {

		Optional<Account> optionalAccount = accountRepository.findByAccount(chargeDto.accountNum());

		if (optionalAccount.isPresent()) {
			Account account = optionalAccount.get();

			account.setAmount(chargeDto.chargeMoney());
		} else {
			throw new BaseException(NotFountAccountException.NOT_FOUND_ACCOUNT);
		}
	}

	@Transactional
	public void craeteSavingAccount(String userId, SavingAccountPwDto savingAccountPwDto) {
		Optional<User> userOptional = userRepository.findByUserId(userId);
		userOptional.orElseThrow(() -> new BaseException(NotFoundException.NOT_FOUND_USER));

		User user = userOptional.get();
		Account account = Account.builder()
			.accountNum(randomAccountNum)
			.type(AccountType.SAVING_ACCOUNT)
			.accountPw(savingAccountPwDto.accountPw())
			.amount(0)
			.user(user)
			.build();

		accountRepository.save(account);
	}

	@Transactional(isolation = Isolation.REPEATABLE_READ)
	public void sendSavingAccount(String userId, SendDto sendDto) {
		Optional<Account> optionalAccount = accountRepository.findByAccount(sendDto.accountNum());
		Optional<Account> mainAccount = accountRepository.findByMainAccount(userId, AccountType.MAIN_ACCOUNT);

		optionalAccount.orElseThrow(() -> new BaseException(NotFountAccountException.NOT_FOUND_ACCOUNT));

		Account main = mainAccount.get();

		if (main.getAccountPw() == sendDto.accountPw()) {
			Account saving = optionalAccount.get();
			int checkMoney = main.getAmount() - sendDto.sendToMoney();

			if (checkMoney > 0) {
				main.reduceAmount(sendDto.sendToMoney());
				saving.increaseAmount(sendDto.sendToMoney());
			} else {
				throw new BaseException(MainAccountException.SHORT_MONEY);
			}
		} else {
			throw new BaseException(NotFountAccountException.NOT_MATCH_ACCOUNT);
		}
	}

	// 메인 계좌 -> 다른 유저의 메인 계좌로 송금 서비스
	@Transactional(isolation = Isolation.REPEATABLE_READ)
	public boolean sendOtherAccount(String userId,SendDto sendDto) {
		Optional<Account> optionalMyAccount = accountRepository.findByMainAccount(userId, AccountType.MAIN_ACCOUNT);
		Optional<Account> optionalAccount = accountRepository.findByAccount(sendDto.accountNum());

		optionalMyAccount.orElseThrow(() -> new BaseException(NotFountAccountException.NOT_MATCH_ACCOUNT));
		optionalAccount.orElseThrow(() -> new BaseException(NotFountAccountException.NOT_FOUND_ACCOUNT));

		Account mainAccount = optionalMyAccount.get();
		Account otherAccount = optionalAccount.get();
		int lackingMoney = sendDto.sendToMoney() - mainAccount.getAmount();

		if (mainAccount.getAmount() > sendDto.sendToMoney()) {
			otherAccount.increaseAmount(sendDto.sendToMoney());
			mainAccount.reduceAmount(sendDto.sendToMoney());
			return true;
		} else {
			int chargeMoney = (int)(Math.ceil(lackingMoney / 10000.0) * 10000);
			chargeMainAccount(new ChargeDto(mainAccount.getAccountNum(), chargeMoney));
		}
		return false;
	}
}
