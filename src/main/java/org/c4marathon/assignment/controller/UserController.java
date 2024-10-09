package org.c4marathon.assignment.controller;

import org.c4marathon.assignment.Dto.TransferRequestDto;
import org.c4marathon.assignment.Dto.UserRequestDto;
import org.c4marathon.assignment.Dto.UserResponseDto;
import org.c4marathon.assignment.service.QueueService;
import org.c4marathon.assignment.service.UserService;
import org.springframework.http.ResponseEntity;
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
	private final QueueService queueService;

	//사용자 회원가입
	@PostMapping("/register")
	public ResponseEntity<UserResponseDto> registerUser(@RequestBody UserRequestDto userRequestDto) {
		UserResponseDto responseDto = userService.registerUser(userRequestDto);
		return ResponseEntity.ok(responseDto);
	}

	//외부 계좌에서 사용자 계좌로 입금(메인 계좌)
	@PostMapping("/{userId}/transfer-from-external/{externalUserId}")
	public ResponseEntity<String> transferFromExternal(
		@PathVariable(value = "userId") Long userId,    //바인딩하면 좋음
		@PathVariable(value = "externalUserId") Long externalUserId,
		@RequestParam int money) {
		TransferRequestDto request = new TransferRequestDto(userId, externalUserId, money);
		queueService.addToQueue(request);
		return ResponseEntity.ok("이체 요청이 접수되었습니다.");
	}
}
