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
	public boolean chargeMainAccount(ChargeDto chargeDto) {

		Optional<Account> optionalAccount = accountRepository.findByAccount(chargeDto.accountNum());

		if (optionalAccount.isPresent()) {
			Account account = optionalAccount.get();

			account.setAmount(chargeDto.chargeMoney());
			return true;
		}

		return false;
	}

	@Transactional(isolation = Isolation.REPEATABLE_READ)
	public boolean craeteSavingAccount(Long userId, SavingAccountPwDto savingAccountPwDto) {
		Optional<User> userOptional = userRepository.findByUserId(String.valueOf(userId));

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
			return true;
		} else {
			return false;
		}
	}

	@Transactional(isolation = Isolation.SERIALIZABLE)
	public boolean sendSavingAccount(Long userId, SendDto sendDto) {
		Optional<Account> optionalAccount = accountRepository.findByAccount(sendDto.accountNum());
		Optional<Account> optionalMyAccount = accountRepository.findByMyAccount(sendDto.accountPw());
		Optional<Account> mainAccount = accountRepository.findByUser_IdAndType(userId, AccountType.MAIN_ACCOUNT);

		optionalAccount.orElseThrow(() -> new BaseException(NotFountAccountException.NOT_FOUNT_ACCOUNT));
		optionalMyAccount.orElseThrow(() -> new BaseException(NotFountAccountException.NOT_MATCH_ACCOUNT));

		Account main = mainAccount.get();
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
}
