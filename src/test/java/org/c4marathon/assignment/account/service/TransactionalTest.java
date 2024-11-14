package org.c4marathon.assignment.account.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.c4marathon.assignment.account.domain.AccountType.MAIN_ACCOUNT;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.c4marathon.assignment.account.domain.Account;
import org.c4marathon.assignment.account.dto.request.SendToOthersRequestDto;
import org.c4marathon.assignment.account.repository.AccountRepository;
import org.c4marathon.assignment.common.fixture.AccountFixture;
import org.c4marathon.assignment.common.fixture.UserFixture;
import org.c4marathon.assignment.user.domain.User;
import org.c4marathon.assignment.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TransactionalTest {

    @BeforeEach
    void setUp() {
        accountRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountService accountService;

    @DisplayName("")
    @Test
    void depositWithOneHundredWithPessimisticLock() throws InterruptedException {
        // given
        int threadCount = 10000;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        User owner = UserFixture.basicUser();
        userRepository.save(owner);
        Account mainAccount = AccountFixture.accountWithTypeAndAmount(owner, MAIN_ACCOUNT, 100_000);
        Account savedAccount = accountRepository.save(mainAccount);
        SendToOthersRequestDto requestDto = new SendToOthersRequestDto(
                savedAccount.getId(),
                100_000
        );

        long startTime = System.currentTimeMillis();

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    accountService.deposit(mainAccount.getId(), 100_000, requestDto);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        long endTime = System.currentTimeMillis();
        System.out.println("Execution time: " + (endTime - startTime) + " ms");
        Account findAccount = accountRepository.findById(savedAccount.getId()).get();

        // then
        assertThat(findAccount.getAmount()).isEqualTo(1_000_100_000);
    }
}
