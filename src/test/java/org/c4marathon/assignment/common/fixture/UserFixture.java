package org.c4marathon.assignment.common.fixture;

import static org.c4marathon.assignment.user.domain.UserRole.*;

import org.c4marathon.assignment.user.domain.User;
import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserFixture {

	public static User basicUser() {
		return User.builder()
			.email("abc@mini.com")
			.name("김미니")
			.password("mini1234")
			.role(USER)
			.build();
	}

	public static User userWithEncodingPassword(PasswordEncoder passwordEncoder) {
		return User.builder()
			.email("abc@mini.com")
			.name("김미니")
			.password(passwordEncoder.encode("mini1234"))
			.role(USER)
			.build();
	}
}
