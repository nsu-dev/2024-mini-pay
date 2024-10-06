package org.c4marathon.assignment.Dto;

import lombok.Getter;
import lombok.NonNull;

@Getter
public class UserRequestDto {
	@NonNull
	private Long userId;
	private String password;
	private String name;
	private String registrationNum;

	public UserRequestDto(Long userId, String password, String name, String registrationNum) {
		this.userId = userId;
		this.password = password;
		this.name = name;
		this.registrationNum = registrationNum;
	}

	public static UserRequestDto of(Long userId, String password, String name, String registrationNum) {
		return new UserRequestDto(userId, password, name, registrationNum);
	}
}
