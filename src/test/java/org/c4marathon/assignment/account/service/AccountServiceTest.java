package org.c4marathon.assignment.account.service;

import org.c4marathon.assignment.domain.account.repository.AccountRepository;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
public class AccountService {
	@MockBean
	private AccountRepository accountRepository;
}
