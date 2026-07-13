package org.example.common.response;

import lombok.Getter;

@Getter
public enum ResponseCode {

    SUCCESS(200, "success"),
    SYSTEM_ERROR(500, "system error");

    private final int code;
    private final String message;

    ResponseCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}