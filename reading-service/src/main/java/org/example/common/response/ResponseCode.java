package org.example.common.response;

import lombok.Getter;

@Getter
public enum ResponseCode {

    SUCCESS(200, "success"),
    BAD_REQUEST(400, "bad request"),
    UNAUTHORIZED(401, "unauthorized"),
    FORBIDDEN(403, "forbidden"),
    NOT_FOUND(404, "not found"),
    SYSTEM_ERROR(500, "system error");

    private final int code;
    private final String message;

    ResponseCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
