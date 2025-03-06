package com.sphere.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GatewayExceptionCode {


    SUCCESS("00", "Successful"),
    IN_PROGRESS("01", "Request In Progress"),

    BAD_REQUEST("10", "Bad Request"),
    NOT_FOUND("11", "Not Found"),
    UNAUTHORIZED("12", "Unauthorized"),
    TOO_MANY_REQUESTS("13", "Too Many Requests"),
    FORBIDDEN("14", "Forbidden"),

    //add here

    SERVER_ERROR("99", "Internal Server Error");


    private final String code;
    private final String message;
}
