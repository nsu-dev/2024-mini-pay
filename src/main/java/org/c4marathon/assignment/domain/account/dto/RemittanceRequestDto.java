package org.c4marathon.assignment.domain.account.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RemittanceRequestDto {
	private Long accountNum;
	private Long remittanceAmount;
}
