package org.c4marathon.assignment.domain.account.entity.responsemsg;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public enum CreateResponseMsg {
	SUCCESS("계좌생성 성공!"),
	FAIL("계좌생성 실패"),
	NOUSER("유저 정보가 없습니다."),
	INVALID_ACCOUNT_TYPE("잘못된 계좌 유형입니다."),
	ACCOUNT_SERVER_ERROR("서버 에러 입니다. 다시 시도 부탁드립니다.");

	private final String responseMsg;
}
