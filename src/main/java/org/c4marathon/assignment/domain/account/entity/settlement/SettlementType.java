package org.c4marathon.assignment.domain.account.entity.settlement;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
public enum SettlementType {
	EQUALS,   // 1/n 방식
	RANDOM    // 랜덤 방식
}
