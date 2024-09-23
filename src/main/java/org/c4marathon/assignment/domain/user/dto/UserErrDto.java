package org.c4marathon.assignment.domain.user.dto;

import org.springframework.http.HttpStatus;

public record UserErrDto(int responseStatus, String responseErrMsg, HttpStatus httpStatus) {
}
