package org.example.common.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResponseResult<T> {
    //    {
//        code: 200,
//        message: "success",
//        data: { }
//    }
    private int code;
    private String message;
    private T data;

    public static <T> ResponseResult<T> success(){
        return success(ResponseCode.SUCCESS,null);
    }

    public static <T> ResponseResult<T> success(T data) {
        return success(ResponseCode.SUCCESS,data);
    }

    public static <T> ResponseResult<T> success(ResponseCode code,T data) {
        return build(code,data);
    }

    public  static <T> ResponseResult<T> fail(){
        return fail(ResponseCode.SYSTEM_ERROR,null);
    }

    public  static <T> ResponseResult<T> fail(ResponseCode code) {
        return fail(code,null);
    }

    public static <T> ResponseResult<T> fail(ResponseCode code, T data) {
        return build(code,data);
    }

    private static <T> ResponseResult<T> build(ResponseCode responseCode, T data) {
        return new ResponseResult<T>(responseCode.getCode(),responseCode.getMessage(),data);
    }
}

