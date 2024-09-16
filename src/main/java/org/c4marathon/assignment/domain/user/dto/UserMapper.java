package org.c4marathon.assignment.domain.user.dto;

import org.c4marathon.assignment.domain.user.entity.User;
import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@RequiredArgsConstructor
public class UserMapper {
	private final PasswordEncoder passwordEncoder;

	public static User toUser(UserDto userDto, PasswordEncoder passwordEncoder) {
		return User.builder()
			.userPhone(userDto.getUserPhone())
			.userPassword(passwordEncoder.encode(userDto.getUserPassword()))
			.userName(userDto.getUserName())
			.userBirth(userDto.getUserBirth())
			.build();
	}
}
