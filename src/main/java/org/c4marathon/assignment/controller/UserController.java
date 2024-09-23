package org.c4marathon.assignment.controller;

import org.c4marathon.assignment.Dto.UserRequestDto;
import org.c4marathon.assignment.Dto.UserResponseDto;
import org.c4marathon.assignment.service.AccountService;
import org.c4marathon.assignment.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;
	private final AccountService accountService;

	//사용자 회원가입
	@PostMapping("/register")
	public ResponseEntity<UserResponseDto> registerUser(@RequestBody UserRequestDto userRequestDto) {
		UserResponseDto responseDto = userService.registerUser(userRequestDto);
		return ResponseEntity.ok(responseDto);
	}

	//적금 계좌 추가
	@PostMapping("/{userId}/savings")
	public ResponseEntity<Void> addSavingsAccount(@PathVariable Long userId,
		@RequestParam String type,
		@RequestParam int balance) {
		accountService.addSavingsAccount(userId, type, balance);
		return ResponseEntity.ok().build();
	}

	//사용자 메인 계좌에서 적금 계좌로 송금
	@PostMapping("/{userId}/move-to-savings")
	public ResponseEntity<String> transferToSavings(@PathVariable Long userId,
		@RequestParam Long savingsAccountId,
		@RequestParam int money) {
		boolean success = accountService.transferToSavings(userId, savingsAccountId, money);
		return success ? ResponseEntity.ok("송금 성공") : ResponseEntity.badRequest().body("송금 실패");
	}

	//외부 계좌에서 사용자 계좌로 입금(메인 계좌)
	@PostMapping("/{userId}/transfer-from-external/{externalUserId}")
	public ResponseEntity<String> transferFromExternal(
		@PathVariable Long userId,
		@PathVariable Long externalUserId,
		@RequestParam int money) {
		try {
			boolean success = accountService.transferFromExternalAccount(userId, externalUserId, money);
			return success ? ResponseEntity.ok("이체 성공") : ResponseEntity.badRequest().body("이체 실패");
		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest().body("오류: " + e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류: " + e.getMessage());
		}
	}

	// IllegalArgumentException 처리
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
		return ResponseEntity.badRequest().body("잘못된 요청: " + e.getMessage());
	}

	// 모든 예외 처리
	@ExceptionHandler(Exception.class)
	public ResponseEntity<String> handleGeneralException(Exception e) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 에러: " + e.getMessage());
	}
}
