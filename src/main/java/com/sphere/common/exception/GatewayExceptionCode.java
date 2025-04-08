package com.sphere.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GatewayExceptionCode {


    SUCCESS("00", "Successful"),
    IN_PROGRESS("01", "Request In Progress"),

    BAD_REQUEST("1010", "Bad Request"),
    NOT_FOUND("1011", "Not Found"),
    UNAUTHORIZED("1012", "Unauthorized"),
    TOO_MANY_REQUESTS("1013", "Too Many Requests"),
    FORBIDDEN("1014", "Forbidden"),

    //add here

    SERVER_ERROR("9090", "Internal Server Error");


    private final String code;
    private final String message;
}
