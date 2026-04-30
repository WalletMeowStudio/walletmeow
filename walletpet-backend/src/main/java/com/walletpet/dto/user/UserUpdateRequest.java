package com.walletpet.dto.user;

import lombok.Data;

@Data
public class UserUpdateRequest {
    private String userName;
    private String password;
}
