package org.c4marathon.assignment.domain.user.dto;

import org.c4marathon.assignment.domain.user.entity.User;
import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserMapper {

	public static User toUser(UserDto userDto, PasswordEncoder passwordEncoder) {
		return User.builder()
			.userPhone(userDto.userPhone())
			.userPassword(passwordEncoder.encode(userDto.userPassword()))
			.userName(userDto.userName())
			.userBirth(userDto.userBirth())
			.build();
	}
}
