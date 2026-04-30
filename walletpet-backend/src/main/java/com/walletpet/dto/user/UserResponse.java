package com.walletpet.dto.user;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserResponse {

    private String userId;

    private String userName;

    private String role;

    private LocalDateTime createdAt;
}