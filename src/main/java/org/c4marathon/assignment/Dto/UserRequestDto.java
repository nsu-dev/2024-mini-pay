package org.c4marathon.assignment.Dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserRequestDto {
	private String password;
	private String name;
	private int registrationNum;

	@Builder
	public UserRequestDto(String password, String name, int registrationNum) {
		this.password = password;
		this.name = name;
		this.registrationNum = registrationNum;
	}
}
