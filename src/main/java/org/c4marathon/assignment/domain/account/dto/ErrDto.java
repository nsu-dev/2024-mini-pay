package org.c4marathon.assignment.domain.account.dto;

import org.springframework.http.HttpStatus;

public record ErrDto(int responseStatus, String responseErrMsg, HttpStatus httpStatus) {
}