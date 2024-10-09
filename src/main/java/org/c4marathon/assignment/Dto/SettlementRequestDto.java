package org.c4marathon.assignment.Dto;

import java.util.List;

import org.c4marathon.assignment.domain.SettlementType;

import lombok.Getter;

@Getter
public class SettlementRequestDto {
	private int totalAmount;
	private SettlementType type;
	private List<Long> participants;
}
