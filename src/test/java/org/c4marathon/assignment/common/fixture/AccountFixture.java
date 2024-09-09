package org.c4marathon.assignment.common.fixture;

import static org.c4marathon.assignment.account.domain.AccountType.*;

import org.c4marathon.assignment.account.domain.Account;
import org.c4marathon.assignment.account.domain.AccountType;
import org.c4marathon.assignment.user.domain.User;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AccountFixture {

	public static Account savingAccountWithUser(User user) {
		return Account.builder()
			.type(SAVING_ACCOUNT)
			.amount(0)
			.limitAmount(3_000_000)
			.user(user)
			.build();
	}

	public static Account accountWithTypeAndAmount(User user, AccountType type, int amount) {
		return Account.builder()
			.type(type)
			.amount(amount)
			.limitAmount(3_000_000)
			.user(user)
			.build();
	}
}
