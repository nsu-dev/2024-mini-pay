package org.c4marathon.assignment.domain.account.exception;

import org.springframework.http.HttpStatus;

import java.net.http.HttpClient;
import java.util.HashMap;

public class AccountException extends RuntimeException{
    private final HttpStatus status;
    private final String detail;
}
