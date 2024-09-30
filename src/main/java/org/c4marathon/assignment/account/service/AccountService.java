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

	@Scheduled(cron = "0 0 0 * * *")
	@Transactional(isolation = Isolation.SERIALIZABLE)
	public void resetLimitAccount() {
		List<Account> accounts = accountRepository.findAll();
		for (Account account : accounts) {
			account.resetLimitAccount();
		}
	}

	@Transactional(isolation = Isolation.REPEATABLE_READ)
	public void chargeMainAccount(ChargeDto chargeDto) {

		Optional<Account> optionalAccount = accountRepository.findByAccount(chargeDto.accountNum());

		if (optionalAccount.isPresent()) {
			Account account = optionalAccount.get();

			account.setAmount(chargeDto.chargeMoney());
		}else {
			throw new BaseException(NotFountAccountException.NOT_FOUNT_ACCOUNT);
		}
	}

	@Transactional
	public void craeteSavingAccount(String userId, SavingAccountPwDto savingAccountPwDto) {
		Optional<User> userOptional = userRepository.findByUserId(userId);

		if (userOptional.isPresent()) {
			User user = userOptional.get();
			Account account = Account.builder()
				.accountNum(randomAccountNum)
				.type(AccountType.SAVING_ACCOUNT)
				.accountPw(savingAccountPwDto.accountPw())
				.amount(0)
				.user(user)
				.build();

			accountRepository.save(account);
		} else {
			throw new BaseException(NotFoundException.NOT_FOUND_USER);
		}
	}

	@Transactional(isolation = Isolation.REPEATABLE_READ)
	public void sendSavingAccount(String userId, SendDto sendDto) {
		Optional<Account> optionalAccount = accountRepository.findByAccount(sendDto.accountNum());
		Optional<Account> mainAccount = accountRepository.findByMainAccount(userId, AccountType.MAIN_ACCOUNT);

		optionalAccount.orElseThrow(() -> new BaseException(NotFountAccountException.NOT_FOUNT_ACCOUNT));

		Account main = mainAccount.get();

		if (main.getAccountPw() == sendDto.accountPw()) {
			Account saving = optionalAccount.get();
			int checkMoney = main.getAmount() - sendDto.sendMoney();

			if (checkMoney > 0) {
				main.reduceAmount(sendDto.sendMoney());
				saving.increaseAmount(sendDto.sendMoney());
				accountRepository.save(main);
				accountRepository.save(saving);
			} else {
				throw new BaseException(MainAccountException.SHORT_MONEY);
			}
		} else {
			throw new BaseException(NotFountAccountException.NOT_MATCH_ACCOUNT);
		}
	}
}
