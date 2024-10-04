package org.c4marathon.assignment.account.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentType {
	SPLIT_EQUALLY("1/n방식"),
	RANDOM("랜덤방식");

	private final String type;
}
