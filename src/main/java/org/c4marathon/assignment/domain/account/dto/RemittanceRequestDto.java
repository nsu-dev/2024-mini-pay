package org.c4marathon.assignment.domain.account.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RemittanceRequestDto {
	private String accountNum;
	private String remittanceAmount;
	private String sender;
}
