package org.example.common.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseResult<T> {
    private int code;
    private String message;
    private T data;

    public static <T> ResponseResult<T> success() {
        return success(null);
    }

    public static <T> ResponseResult<T> success(T data) {
        return new ResponseResult<>(ResponseCode.SUCCESS.getCode(), ResponseCode.SUCCESS.getMessage(), data);
    }

    public static <T> ResponseResult<T> fail(ResponseCode code) {
        return new ResponseResult<>(code.getCode(), code.getMessage(), null);
    }

    public static <T> ResponseResult<T> fail(ResponseCode code, String message) {
        return new ResponseResult<>(code.getCode(), message, null);
    }
}
