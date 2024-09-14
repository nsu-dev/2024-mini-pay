package org.c4marathon.assignment.service;

import java.time.LocalDate;

import org.c4marathon.assignment.Dto.UserRequestDto;
import org.c4marathon.assignment.Dto.UserResponseDto;
import org.c4marathon.assignment.domain.Account;
import org.c4marathon.assignment.domain.User;
import org.c4marathon.assignment.repository.AccountRepository;
import org.c4marathon.assignment.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Service
@RequiredArgsConstructor //@Autowired 말고 생성자 주입을 임의의 코드 없이 자동으로 설정
public class UserService {
	private final UserRepository userRepository;
	private final AccountRepository accountRepository;

	//사용자 회원가입(메인 계좌 생성)
	@Transactional(isolation = Isolation.REPEATABLE_READ)
	public UserResponseDto registerUser(UserRequestDto userRequestDto) {
		// User 객체를 빌더 패턴으로 생성
		User user = User.builder()
			.password(userRequestDto.getPassword())
			.name(userRequestDto.getName())
			.registrationNum(userRequestDto.getRegistrationNum())
			.build();

		// 메인 계좌 설정
		Account mainAccount = new Account("Main Account", 0, user);
		user.setMainAccount(mainAccount);

		// 저장
		User savedUser = userRepository.save(user);    // userId는 save() 후 자동 할당됨

		// DTO 응답
		return UserResponseDto.builder()
			.userId(savedUser.getUserId())    // 저장된 객체에서 userId 가져오기
			.name(savedUser.getName())
			.registrationNum(savedUser.getRegistrationNum())
			.build();
	}

	//적금 계좌 추가
	@Transactional(isolation = Isolation.REPEATABLE_READ)
	public void addSavingsAccount(Long userId, String type, int balance) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

		//적금 계좌 생성
		Account savingsAccount = new Account(type, balance, user);

		//적금 계좌를 User 객체에 추가
		user.addSavingAccount(savingsAccount);

		//적금 계좌 저장
		accountRepository.save(savingsAccount);
	}

	// 메인 계좌에서 적금 계좌로 송금
	@Transactional(isolation = Isolation.REPEATABLE_READ)
	public boolean transferToSavings(Long userId, Long savingsAccountId, int money) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

		Account mainAccount = user.getMainAccount();
		Account savingsAccount = accountRepository.findById(savingsAccountId)
			.orElseThrow(() -> new IllegalArgumentException("적금 계좌를 찾을 수 없습니다."));

		// 메인 계좌에서 출금
		mainAccount.withdraw(money, LocalDate.now());

		// 적금 계좌로 입금
		savingsAccount.deposit(money);

		// 계좌 저장
		accountRepository.save(mainAccount);
		accountRepository.save(savingsAccount);

		return true;
	}

}
