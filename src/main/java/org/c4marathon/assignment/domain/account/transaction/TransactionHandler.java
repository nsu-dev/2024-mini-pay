package org.c4marathon.assignment.domain.account.transaction;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TransactionHandler {
	@Transactional(isolation = Isolation.REPEATABLE_READ)
	public void runInRepeatableTransaction(Action action) {
		action.act();
	}

	@Transactional(isolation = Isolation.READ_COMMITTED)
	public void runInCommittedTransaction(Action action) {
		action.act();
	}

	public interface Action {
		void act();
	}
}
