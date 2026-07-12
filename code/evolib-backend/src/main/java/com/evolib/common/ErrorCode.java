package com.evolib.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    
    SUCCESS(0, "操作成功"),
    
    OVERDUE(4001, "读者存在超期未还图书，无法借书"),
    BORROW_LIMIT_EXCEEDED(4002, "读者已达到借阅上限"),
    STOCK_NOT_ENOUGH(4003, "图书可借数量不足"),
    READER_NOT_FOUND(4004, "读者不存在"),
    BOOK_NOT_FOUND(4005, "图书不存在"),
    NOT_BORROWED_BY_READER(4006, "该书未被该读者借阅"),
    INVALID_PARAM(4007, "参数格式不合法"),
    BOOK_NOT_ACTIVE(4008, "图书已下架，不可操作"),
    DUPLICATE_BORROW(4009, "读者已借阅此书");
    
    private final int code;
    private final String message;
}