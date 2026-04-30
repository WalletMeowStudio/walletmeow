package com.walletpet.dto.auth;

import lombok.Data;

@Data
public class LoginRequest {

    private String userName;
    private String password;
}