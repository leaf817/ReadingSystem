package org.example.common.exception;

import lombok.Getter;
import org.example.common.response.ResponseCode;

@Getter
public class BusinessException extends RuntimeException {
    private final ResponseCode responseCode;

    public BusinessException(final ResponseCode responseCode) {
        super(responseCode.getMessage());
        this.responseCode = responseCode;
    }

    public BusinessException(ResponseCode responseCode, String message) {
        super(message);
        this.responseCode = responseCode;
    }

    public BusinessException(ResponseCode responseCode, String message, Throwable cause) {
        super(message, cause);
        this.responseCode = responseCode;
    }
}
