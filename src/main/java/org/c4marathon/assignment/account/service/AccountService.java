package org.c4marathon.assignment.account.service;

import static org.c4marathon.assignment.account.domain.AccountType.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

import org.c4marathon.assignment.account.domain.Account;
import org.c4marathon.assignment.account.domain.AccountType;
import org.c4marathon.assignment.account.dto.AccountMapper;
import org.c4marathon.assignment.account.dto.request.ChargeRequestDto;
import org.c4marathon.assignment.account.dto.request.SendRequestDto;
import org.c4marathon.assignment.account.dto.response.AccountResponseDto;
import org.c4marathon.assignment.account.dto.response.ChargeResponseDto;
import org.c4marathon.assignment.account.dto.response.SavingAccountResponseDto;
import org.c4marathon.assignment.account.dto.response.SendResponseDto;
import org.c4marathon.assignment.account.exception.AccountErrorCode;
import org.c4marathon.assignment.account.repository.AccountRepository;
import org.c4marathon.assignment.common.exception.runtime.BaseException;
import org.c4marathon.assignment.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {

	private final AccountRepository accountRepository;
	public static final String TIME_ZONE = "Asia/Seoul";

	@Transactional
	public SavingAccountResponseDto generateSavingAccount(User user) {
		Account savingAccount = Account.builder()
			.type(SAVING_ACCOUNT)
			.amount(0)
			.limitAmount(3_000_000)
			.user(user)
			.build();

		accountRepository.save(savingAccount);
		return AccountMapper.toSavingAccountResponseDto(savingAccount);
	}

	@Transactional(readOnly = true)
	public List<AccountResponseDto> getAccounts(User user) {
		List<Account> accounts = accountRepository.findAllByUser(user);

		return AccountMapper.toAccountResponseDtos(accounts);
	}

	@Transactional(isolation = Isolation.SERIALIZABLE)
	public SendResponseDto sendMoney(User user, SendRequestDto sendRequestDto) {
		int sendToMoney = sendRequestDto.sendToMoney();

		Account toAccount = findAccount(sendRequestDto.toAccountId(), sendRequestDto.toAccountType());
		Account fromAccount = findAccount(sendRequestDto.fromAccountId(), sendRequestDto.fromAccountType());

		if (!verifyAccountByUser(user, toAccount) || !verifyAccountByUser(user, fromAccount)) {
			throw new BaseException(AccountErrorCode.NOT_AUTHORIZED_ACCOUNT);
		}

		toAccount.decreaseAmount(sendToMoney);
		fromAccount.increaseAmount(sendToMoney);

		return AccountMapper.toSendResponseDto(toAccount, fromAccount);
	}

	private Account findAccount(Long accountId, String type) {
		AccountType findAccountType = from(type);

		return accountRepository.findByIdAndType(accountId, findAccountType)
			.orElseThrow(() -> new BaseException(AccountErrorCode.NOT_FOUND_ACCOUNT));
	}

	private boolean verifyAccountByUser(User user, Account account) {
		return user.equals(account.getUser());
	}

	@Transactional(isolation = Isolation.SERIALIZABLE)
	public ChargeResponseDto chargeMainAccount(User user, ChargeRequestDto requestDto) {

		Account findAccount = accountRepository.findById(requestDto.accountId()).orElseThrow(
			() -> new BaseException(AccountErrorCode.NOT_FOUND_ACCOUNT));

		if (!verifyAccountByUser(user, findAccount)) {
			throw new BaseException(AccountErrorCode.NOT_AUTHORIZED_ACCOUNT);
		}

		if (!verifyMainAccount(findAccount)) {
			throw new BaseException(AccountErrorCode.NOT_ACCESS_CHARGE);
		}

		verifyLastChargeDate(findAccount);

		findAccount.chargeAmount(requestDto.chargeAmount());

		return AccountMapper.toChargeResponseDto(findAccount);
	}

	private void verifyLastChargeDate(Account account) {
		if (!Objects.equals(account.getLastChargeDate(), LocalDate.now(ZoneId.of(TIME_ZONE)))) {
			account.resetLimitAmount();
		}
	}

	private boolean verifyMainAccount(Account account) {
		return Objects.equals(account.getType().getType(), MAIN_ACCOUNT.getType());
	}
}
