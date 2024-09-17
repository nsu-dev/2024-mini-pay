package org.c4marathon.assignment.Dto;

import lombok.Getter;

@Getter
public class UserRequestDto {
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
}
