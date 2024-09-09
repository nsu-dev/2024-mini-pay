package org.c4marathon.assignment.user.controller;

import org.c4marathon.assignment.user.dto.request.JoinRequestDto;
import org.c4marathon.assignment.user.dto.request.LoginRequestDto;
import org.c4marathon.assignment.user.dto.response.JoinResponseDto;
import org.c4marathon.assignment.user.dto.response.LoginResponseDto;
import org.c4marathon.assignment.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@PostMapping("/api/user/join")
	public ResponseEntity<JoinResponseDto> join(
		@Valid
		@RequestBody JoinRequestDto joinRequestDto
	) {
		JoinResponseDto joinResponseDto = userService.join(joinRequestDto);
		return ResponseEntity.ok(joinResponseDto);
	}

	@PostMapping("/api/user/login")
	public ResponseEntity<LoginResponseDto> login(
		@Valid
		@RequestBody LoginRequestDto loginRequestDto
	) {
		LoginResponseDto loginResponseDto = userService.login(loginRequestDto);

		return ResponseEntity.ok(loginResponseDto);
	}
}
