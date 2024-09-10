package org.c4marathon.assignment.domain.user.dto;

import org.c4marathon.assignment.domain.user.entity.User;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserMapper {
	public static User toUser(UserDto userDto) {
		return User.builder()
			.userPhone(userDto.getUserPhone())
			.userPassword(userDto.getUserPassword())
			.userName(userDto.getUserName())
			.userBirth(userDto.getUserBirth())
			.build();
	}
}
