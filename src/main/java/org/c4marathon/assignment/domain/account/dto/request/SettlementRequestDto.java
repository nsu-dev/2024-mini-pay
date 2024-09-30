package org.c4marathon.assignment.domain.account.dto.request;

import java.util.List;

import org.c4marathon.assignment.domain.account.entity.settlement.SettlementType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SettlementRequestDto(
	@NotNull(message = "정산액은 공백일 수 없습니다.")
	@Min(value = 1, message = "정산액은 0원 이하일 수 없습니다.")
	Long settlementAmount,
	@NotBlank(message = "정산유형은 공백일 수 없습니다.")
	SettlementType settlementType,
	@NotNull(message = "정산 대상자는 공백일 수 없습니다.")
	@Size(min = 1, message = "정산 대상자는 1명 이상이어야 합니다.")
	List<String> settlementTargetList
) {
}
