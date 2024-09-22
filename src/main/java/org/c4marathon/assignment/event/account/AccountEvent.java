package org.c4marathon.assignment.event.account;

import org.c4marathon.assignment.user.domain.User;

import lombok.Getter;

@Getter
public class AccountEvent {

	private User user;

	public AccountEvent(User user) {
		this.user = user;
	}
}
