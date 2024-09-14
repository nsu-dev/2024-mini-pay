package org.c4marathon.assignment.Dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserRequestDto {
	private Long userId;
	private String password;
	private String name;
	private String registrationNum;

	@Builder
	public UserRequestDto(Long userId, String password, String name, String registrationNum) {
		this.userId = userId;
		this.password = password;
		this.name = name;
		this.registrationNum = registrationNum;
	}
}
