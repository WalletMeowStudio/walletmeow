package com.walletpet.dto.user;

import lombok.Data;

@Data
public class UserRegisterRequest {

    private String userName;

    private String password;

    private String petName;
}