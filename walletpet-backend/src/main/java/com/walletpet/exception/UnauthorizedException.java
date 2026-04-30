package com.walletpet.exception;

//登入驗證失敗、token 無效、未登入時使用
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}