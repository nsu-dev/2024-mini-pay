package org.c4marathon.assignment.domain.account.transaction;

import org.springframework.stereotype.Component;

@Component
public class TransactionHandler {
	public void runInTransaction(Action action) {
		action.act();
	}

	public interface Action {
		void act();
	}
}
