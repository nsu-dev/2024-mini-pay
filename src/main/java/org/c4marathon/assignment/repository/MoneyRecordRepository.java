package org.c4marathon.assignment.repository;

import org.c4marathon.assignment.domain.Account;
import org.c4marathon.assignment.domain.MoneyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MoneyRecordRepository extends JpaRepository<MoneyRecord, Long> {
    //특정 계좌의 오늘 출금한 금액을 조회!
    List<MoneyRecord> findByAccountAndDate(Account account, LocalDate date);
}
