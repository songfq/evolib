package com.evolib.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    private int code;
    private String message;
    private T data;
    
    public static <T> Result<T> ok(T data) {
        return new Result<>(0, "success", data);
    }
    
    public static <T> Result<T> fail(ErrorCode code) {
        return new Result<>(code.getCode(), code.getMessage(), null);
    }
    
    public static <T> Result<T> fail(ErrorCode code, String detail) {
        return new Result<>(code.getCode(), detail, null);
    }
}