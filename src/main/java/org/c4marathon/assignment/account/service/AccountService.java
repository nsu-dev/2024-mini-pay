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
import org.c4marathon.assignment.account.dto.request.SendToOthersRequestDto;
import org.c4marathon.assignment.account.dto.response.AccountResponseDto;
import org.c4marathon.assignment.account.dto.response.ChargeResponseDto;
import org.c4marathon.assignment.account.dto.response.SavingAccountResponseDto;
import org.c4marathon.assignment.account.dto.response.SendResponseDto;
import org.c4marathon.assignment.account.dto.response.SendToOthersResponseDto;
import org.c4marathon.assignment.account.exception.AccountErrorCode;
import org.c4marathon.assignment.account.repository.AccountRepository;
import org.c4marathon.assignment.common.exception.runtime.BaseException;
import org.c4marathon.assignment.event.account.WithdrawalFailEvent;
import org.c4marathon.assignment.user.domain.User;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {

	public static final String TIME_ZONE = "Asia/Seoul";
	private final AccountRepository accountRepository;
	private final ApplicationEventPublisher eventPublisher;

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

		Account findAccount = accountRepository.findById(requestDto.accountId())
			.orElseThrow(() -> new BaseException(AccountErrorCode.NOT_FOUND_ACCOUNT));

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

	public SendToOthersResponseDto sendToOthers(
		Long othersAccountId,
		String othersAccountType,
		User user,
		SendToOthersRequestDto requestDto
	) {
		int sendToMoney = withdrawal(user, requestDto);
		return deposit(othersAccountId, othersAccountType, sendToMoney, requestDto);
	}

	@Transactional(isolation = Isolation.REPEATABLE_READ)
	public int withdrawal(User user, SendToOthersRequestDto requestDto) {
		Account userAccount = findAccount(requestDto.accountId(), requestDto.accountType());

		if (!verifyAccountByUser(user, userAccount)) {
			throw new BaseException(AccountErrorCode.NOT_AUTHORIZED_ACCOUNT);
		}

		return userAccount.decreaseAmount(requestDto.sendAmount());
	}

	@Transactional(isolation = Isolation.REPEATABLE_READ)
	public SendToOthersResponseDto deposit(
		Long othersAccountId,
		String othersAccountType,
		int sendToMoney,
		SendToOthersRequestDto requestDto
	) {
		try {
			Account othersAccount = findAccount(othersAccountId, othersAccountType);

			if (!verifyMainAccount(othersAccount)) {
				throw new BaseException(AccountErrorCode.NOT_MAIN_ACCOUNT);
			}

			othersAccount.increaseAmount(sendToMoney);

			return AccountMapper.sendToOthersResponseDto(othersAccount.getUser(), sendToMoney);
		} catch (Exception e) {
			Account userAccount = findAccount(requestDto.accountId(), requestDto.accountType());
			eventPublisher.publishEvent(new WithdrawalFailEvent(userAccount, requestDto.sendAmount()));
			throw new BaseException(AccountErrorCode.FAILED_ACCOUNT_DEPOSIT);
		}
	}
}
