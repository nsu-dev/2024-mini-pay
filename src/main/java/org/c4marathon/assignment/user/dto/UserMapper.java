package org.c4marathon.assignment.user.dto;

import static lombok.AccessLevel.*;
import static org.c4marathon.assignment.user.domain.UserRole.*;

import org.c4marathon.assignment.user.domain.User;
import org.c4marathon.assignment.user.dto.request.JoinRequestDto;
import org.c4marathon.assignment.user.dto.response.JoinResponseDto;
import org.c4marathon.assignment.user.dto.response.LoginResponseDto;
import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public class UserMapper {

	public static User toUserFromJoinRequestDto(JoinRequestDto requestDto, PasswordEncoder passwordEncoder) {
		return User.builder()
			.email(requestDto.email())
			.password(passwordEncoder.encode(requestDto.password()))
			.name(requestDto.name())
			.role(USER)
			.build();
	}

	public static JoinResponseDto toJoinResponseDtoFromUser(User user) {
		return new JoinResponseDto(user.getEmail());
	}

	public static LoginResponseDto toLoginResponseDto(String token) {
		return new LoginResponseDto(token);
	}

}
