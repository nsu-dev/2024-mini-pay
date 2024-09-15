package org.c4marathon.assignment.domain.user.dto;

import org.c4marathon.assignment.domain.user.entity.User;
import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserMapper {
	PasswordEncoder passwordEncoder;

	public static User toUser(UserDto userDto, PasswordEncoder passwordEncoder) {
		return User.builder()
			.userPhone(userDto.getUserPhone())
			.userPassword(passwordEncoder.encode(userDto.getUserPassword()))
			.userName(userDto.getUserName())
			.userBirth(userDto.getUserBirth())
			.build();
	}
}
