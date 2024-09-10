package org.c4marathon.assignment.domain.user.controller;

import org.c4marathon.assignment.domain.user.dto.JoinResponseDto;
import org.c4marathon.assignment.domain.user.dto.LoginRequestDto;
import org.c4marathon.assignment.domain.user.dto.LoginResponseDto;
import org.c4marathon.assignment.domain.user.dto.UserDto;
import org.c4marathon.assignment.domain.user.entity.JoinResponseMsg;
import org.c4marathon.assignment.domain.user.entity.LoginResponseMsg;
import org.c4marathon.assignment.domain.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

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
			if (joinResponseDto.responseMsg().equals(JoinResponseMsg.SUCCESS.getResponseMsg())) {
				return ResponseEntity.ok().body(joinResponseDto);
			} else {
				return ResponseEntity.badRequest().body(joinResponseDto);
			}
		} catch (HttpClientErrorException e) {
			JoinResponseDto joinResponseDto = JoinResponseDto.builder()
				.responseMsg(JoinResponseMsg.DUPLICATIEDFAIL.getResponseMsg())
				.build();
			return ResponseEntity.status(e.getStatusCode()).body(joinResponseDto);
		}

	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponseDto> userLogin(@Valid @RequestBody LoginRequestDto loginRequestDto) {
		try {
			LoginResponseDto loginResponseDto = userService.login(loginRequestDto);
			if (loginResponseDto.responseMsg().equals(LoginResponseMsg.SUCCESS.getResponseMsg())) {
				return ResponseEntity.ok().body(loginResponseDto);
			} else {
				return ResponseEntity.badRequest().body(loginResponseDto);
			}
		} catch (HttpClientErrorException e) {
			LoginResponseDto loginResponseDto = LoginResponseDto.builder()
				.responseMsg(LoginResponseMsg.NOTUSER.getResponseMsg())
				.build();
			return ResponseEntity.status(e.getStatusCode()).body(loginResponseDto);
		}
	}
}
