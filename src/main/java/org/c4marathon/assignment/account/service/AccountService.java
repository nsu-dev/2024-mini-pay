package org.c4marathon.assignment.account.service;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.c4marathon.assignment.account.controller.AccountCalculateController;
import org.c4marathon.assignment.account.domain.Account;
import org.c4marathon.assignment.account.enums.AccountType;
import org.c4marathon.assignment.account.dto.CalculatePaymentDto;
import org.c4marathon.assignment.account.dto.ChargeDto;
import org.c4marathon.assignment.account.dto.SavingAccountPwDto;
import org.c4marathon.assignment.account.dto.SendDto;
import org.c4marathon.assignment.account.enums.PaymentType;
import org.c4marathon.assignment.account.exception.CalculatePaymentTogetherException;
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

	private final AccountCalculateController accountCalculateController;

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

	// 적금 계좌 생성 서비스
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

	// 적금 계좌로 송금할 때 서비스 ( 메인 -> 적금 )
	@Transactional(isolation = Isolation.REPEATABLE_READ)
	public void sendSavingAccount(String userId, SendDto sendDto) {
		Optional<Account> optionalAccount = accountRepository.findByAccount(sendDto.accountNum());
		Optional<Account> mainAccount = accountRepository.findByMainAccount(userId, AccountType.MAIN_ACCOUNT);

		optionalAccount.orElseThrow(() -> new BaseException(NotFountAccountException.NOT_FOUND_ACCOUNT));
		mainAccount.orElseThrow(() -> new BaseException(NotFountAccountException.NOT_FOUND_MAIN_ACCOUNT));

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

	// 메인 계좌로 송금할 때 서비스 ( 메인 -> 다른 사람의 메인 )
	@Transactional(isolation = Isolation.REPEATABLE_READ)
	public void sendOtherAccount(String userId,SendDto sendDto) {
		Optional<Account> optionalMyAccount = accountRepository.findByMainAccount(userId, AccountType.MAIN_ACCOUNT);
		Optional<Account> optionalAccount = accountRepository.findByOtherMainAccount(sendDto.accountNum(), AccountType.MAIN_ACCOUNT);

		optionalMyAccount.orElseThrow(() -> new BaseException(NotFountAccountException.NOT_FOUND_MAIN_ACCOUNT));
		optionalAccount.orElseThrow(() -> new BaseException(NotFountAccountException.NOT_FOUND_OTHER_MAIN_ACCOUNT));

		Account mainAccount = optionalMyAccount.get();
		Account otherAccount = optionalAccount.get();

		int lackingMoney = sendDto.sendToMoney() - mainAccount.getAmount();

		if (mainAccount.getAmount() > sendDto.sendToMoney()) {
			otherAccount.increaseAmount(sendDto.sendToMoney());
			mainAccount.reduceAmount(sendDto.sendToMoney());
		} else {
			int chargeMoney = (int)(Math.ceil(lackingMoney / 10000.0) * 10000);
			chargeMainAccount(new ChargeDto(mainAccount.getAccountNum(), chargeMoney));
		}
	}

	// 정산하기 트랜잭션 나누는 서비스
	public void calculatePaymentTogether(CalculatePaymentDto calculatePaymentDto){
		if (calculatePaymentDto.paymentType() == PaymentType.RANDOM){
			calculatePaymentTogetherTypeRandom(calculatePaymentDto);
		}
		else if (calculatePaymentDto.paymentType() == PaymentType.SPLIT_EQUALLY){
			calculatePaymentTogetherTypeSplit(calculatePaymentDto);
		}else {
			throw new BaseException(CalculatePaymentTogetherException.WRONG_TYPE);
		}
	}

	// A가 B와 C에게 정산 요청을 보낼 때 서비스 로직
	@Transactional
	public void requestSettlement(CalculatePaymentDto calculatePaymentDto) {
		// 정산 요청을 받은 사용자 ID
		List<String> participants = calculatePaymentDto.usersId();

		// 유저에게 SSE 이벤트 발송 (정산 요청 도착)
		for (String participant : participants) {
			accountCalculateController.sendEventToClient(participant, "settlement-request", "정산 요청이 도착했습니다.");
		}
	}

	// B나 C가 정산 요청을 수락하고 정산을 진행할 때
	@Transactional
	public void processSettlement(SendDto sendDto) {
		Optional<Account> optionalMainAccount = accountRepository.findByMainAccount(userId, AccountType.MAIN_ACCOUNT);
		optionalMainAccount.orElseThrow(() -> new BaseException(NotFountAccountException.NOT_FOUND_MAIN_ACCOUNT));

		Account account = optionalMainAccount.get();

		// 정산 금액 처리 로직
		int amount = sendDto.sendToMoney();
		account.reduceAmount(amount);

		// 정산 완료 후 A에게 SSE 이벤트 발송 (정산 완료 알림)
		accountCalculateController.sendEventToClient(sendDto, "settlement-complete", "사용자 " + userId + "가 정산을 완료했습니다.");
	}

	// 정산하기 ( 랜덤타입 )
	@Transactional(isolation = Isolation.REPEATABLE_READ)
	public void calculatePaymentTogetherTypeRandom(CalculatePaymentDto calculatePaymentDto){

	}

	// 정산하기 ( 1/n 타입 )
	@Transactional(isolation = Isolation.REPEATABLE_READ)
	public void calculatePaymentTogetherTypeSplit(CalculatePaymentDto calculatePaymentDto){

	}

}
