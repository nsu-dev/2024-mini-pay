package org.c4marathon.assignment.domain.account.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public enum RLockErrorMsg {
	TOO_MANY_USER("현재 사용자가 너무 많습니다. 다시 시도해주세요.");

	private final String responseMsg;
}
