package com.walletpet.exception;

//查不到資料時使用
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}