package org.c4marathon.assignment.event;

import org.c4marathon.assignment.user.domain.User;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JoinEventDto {
	private User user;
}
