package org.c4marathon.assignment.domain.settlement.dto.response;

import org.springframework.http.HttpStatus;

public record SettlementErrDto(int responseStatus, String responseErrMsg, HttpStatus httpStatus) {
}
