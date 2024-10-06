package org.c4marathon.assignment;

import org.c4marathon.assignment.domain.User;

public class UserFixture {

	// 기본 사용자 생성 메소드
	public static User createUser(Long userId, String name, String password, String registrationNum) {
		return User.builder()
			.userId(userId)
			.name(name)
			.password(password)
			.registrationNum(registrationNum)
			.build();
	}

	// 기본 사용자를 생성하는 메소드
	public static User createDefaultUser() {
		return createUser(1L, "이수경", "lsk123", "123456-789123");
	}

	// 외부 사용자를 생성하는 메소드
	public static User createExternalUser(Long externalUserId) {
		return createUser(externalUserId, "김첨지", "kim123", "234567-890123");
	}
}
