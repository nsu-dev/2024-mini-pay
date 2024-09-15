package org.c4marathon.assignment.domain.user.controller;

import static org.c4marathon.assignment.domain.user.entity.UserErrCode.*;

import org.c4marathon.assignment.domain.user.dto.JoinResponseDto;
import org.c4marathon.assignment.domain.user.dto.LoginRequestDto;
import org.c4marathon.assignment.domain.user.dto.LoginResponseDto;
import org.c4marathon.assignment.domain.user.dto.UserDto;
import org.c4marathon.assignment.domain.user.exception.UserException;
import org.c4marathon.assignment.domain.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
	private final UserService userService;

	@PostMapping("/join")
	public ResponseEntity<JoinResponseDto> userJoin(@Valid @RequestBody UserDto userDto) {
		try {
			JoinResponseDto joinResponseDto = userService.join(userDto);
			return ResponseEntity.ok().body(joinResponseDto);
		} catch (RuntimeException e) {
			throw new UserException(USER_SERVER_ERROR);
		}
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponseDto> userLogin(@Valid @RequestBody LoginRequestDto loginRequestDto,
		HttpServletRequest httpServletRequest) {
		try {
			LoginResponseDto loginResponseDto = userService.login(loginRequestDto, httpServletRequest);
			return ResponseEntity.ok().body(loginResponseDto);
		} catch (RuntimeException e) {
			throw new UserException(USER_SERVER_ERROR);
		}
	}
}
