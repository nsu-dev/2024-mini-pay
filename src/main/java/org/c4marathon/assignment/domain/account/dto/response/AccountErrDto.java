package org.c4marathon.assignment.domain.account.dto.response;

import org.springframework.http.HttpStatus;

public record AccountErrDto(int responseStatus, String responseErrMsg, HttpStatus httpStatus) {
}