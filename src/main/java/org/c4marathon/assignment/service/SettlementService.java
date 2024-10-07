package org.c4marathon.assignment.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.c4marathon.assignment.domain.Settlement;
import org.c4marathon.assignment.domain.SettlementType;
import org.c4marathon.assignment.repository.SettlementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SettlementService {
	private final SettlementRepository settlementRepository;

	public SettlementService(SettlementRepository settlementRepository) {
		this.settlementRepository = settlementRepository;
	}

	//정산 요청을 처리 메서드
	@Transactional
	public Settlement requestSettlement(int totalAmount, SettlementType type, List<Long> participants) {
		//정산 타입에 따른 금액 분배
		List<Integer> amounts = calculateAmounts(totalAmount, type, participants.size());

		//새 정산 요청 생성 후 저장
		Settlement settlement = new Settlement(totalAmount, LocalDate.now(), type, participants, amounts);
		return settlementRepository.save(settlement);
	}

	// 특정 정산 요청을 조회하는 메서드
	@Transactional(readOnly = true)
	public Settlement getSettlement(Long id) {
		return settlementRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("정산 요청을 찾을 수 없습니다."));
	}

	// 정산 완료 처리
	@Transactional
	public void completeSettlement(Long id) {
		Settlement settlement = getSettlement(id);
		settlement.completeSettlement();
		settlementRepository.save(settlement);  // 정산 완료 상태 저장
	}

	// 금액 분배 로직 (1/n 또는 랜덤)
	private List<Integer> calculateAmounts(int totalAmount, SettlementType type, int participantCount) {
		if (participantCount <= 0) {
			throw new IllegalArgumentException("참여자 수는 0보다 커야 합니다.");
		}

		List<Integer> amounts = new ArrayList<>();
		int maxRetryCount = 3;  // 최대 3번 재시도
		int retryCount = 0;

		do {
			switch (type) {
				case EQUA:  // 1/n 정산
					amounts = distributeEqually(totalAmount, participantCount);
					break;

				case RANDOM:  // 랜덤 정산
					amounts = distributeRandomly(totalAmount, participantCount);
					break;

				default:
					throw new IllegalArgumentException("지원하지 않는 정산 방식입니다.");
			}

			// 분배된 금액의 총합이 totalAmount와 일치하는지 확인
			int sum = amounts.stream().mapToInt(Integer::intValue).sum();
			if (sum == totalAmount) {
				return amounts;  // 총합이 일치하면 반환
			}

			retryCount++;
		} while (retryCount < maxRetryCount);

		// 재시도 후에도 합계가 일치하지 않으면 예외 발생
		throw new IllegalStateException("분배된 금액의 총합이 원래 금액과 일치하지 않습니다.");
	}

	// 1/n 정산
	private List<Integer> distributeEqually(int totalAmount, int participantCount) {
		List<Integer> amounts = new ArrayList<>();
		int equalAmount = totalAmount / participantCount;
		int remainder = totalAmount % participantCount;

		// 모든 사람에게 동일한 금액 할당
		for (int i = 0; i < participantCount; i++) {
			amounts.add(equalAmount);
		}

		// 남은 금액을 랜덤하게 할당
		Random random = new Random();
		while (remainder > 0) {
			int randomIndex = random.nextInt(participantCount); // 랜덤하게 인덱스 선택
			amounts.set(randomIndex, amounts.get(randomIndex) + 1); // 해당 인덱스에 1 추가
			remainder--; // 나머지 금액에서 1 차감
		}

		return amounts;
	}

	// 랜덤 정산: 총 금액 내에서 무작위로 분배 (합계는 totalAmount와 동일)
	private List<Integer> distributeRandomly(int totalAmount, int participantCount) {
		List<Integer> amounts = new ArrayList<>();
		Random random = new Random();
		int remainingAmount = totalAmount;

		// 마지막 사람을 제외한 인원에게 랜덤으로 금액 분배
		for (int i = 0; i < participantCount - 1; i++) {
			int randomAmount = random.nextInt(remainingAmount / (participantCount - i) * 2);  // 남은 금액에서 랜덤한 값을 할당
			amounts.add(randomAmount);
			remainingAmount -= randomAmount;
		}

		// 마지막 사람에게 남은 금액 할당
		amounts.add(remainingAmount);

		return amounts;
	}
}
