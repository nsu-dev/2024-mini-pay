package org.c4marathon.assignment.service;

import org.c4marathon.assignment.Dto.UserRequestDto;
import org.c4marathon.assignment.Dto.UserResponseDto;
import org.c4marathon.assignment.domain.Account;
import org.c4marathon.assignment.domain.AccountType;
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
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public UserResponseDto registerUser(UserRequestDto userRequestDto) {
		// User 객체를 빌더 패턴으로 생성
		User user = User.builder()
			.password(userRequestDto.getPassword())
			.name(userRequestDto.getName())
			.registrationNum(userRequestDto.getRegistrationNum())
			.build();

		// 메인 계좌 설정
		Account mainAccount = new Account(AccountType.MAIN, 0, user);
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
}
