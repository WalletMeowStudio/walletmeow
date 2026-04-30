package com.walletpet.exception;

//帳戶相關的邏輯：餘額不足、分類不可修改、帳戶已停用
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}