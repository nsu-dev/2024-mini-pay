package org.c4marathon.assignment.user.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {

	USER("일반회원"),
	ADMIN("운영자");

	private final String role;
}
