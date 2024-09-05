package org.c4marathon.assignment.user.dto;

import static lombok.AccessLevel.*;
import static org.c4marathon.assignment.user.domain.UserRole.*;

import org.c4marathon.assignment.user.domain.User;
import org.c4marathon.assignment.user.dto.request.JoinRequestDto;
import org.c4marathon.assignment.user.dto.response.JoinResponseDto;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public class UserMapper {

	public static User toUserFromJoinRequestDto(JoinRequestDto requestDto) {
		return User.builder()
			.email(requestDto.email())
			.password(requestDto.password())
			.name(requestDto.name())
			.role(USER)
			.build();
	}

	public static JoinResponseDto toJoinResponseDtoFromUser(User user) {
		return new JoinResponseDto(user.getEmail());
	}

}
