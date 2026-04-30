package com.walletpet.service;

import com.walletpet.dto.user.UserRegisterRequest;
import com.walletpet.dto.user.UserResponse;
import com.walletpet.dto.user.UserUpdateRequest;
import com.walletpet.entity.User;

import java.util.Optional;

public interface UserService {

    // 註冊新使用者
    UserResponse registerUser(UserRegisterRequest request);

    // 根據 ID 查詢使用者
    UserResponse findUserById(String userId);
    
    User getUserEntityById(String userId);

    // 根據帳號查詢 (登入用)
    UserResponse findUserByUserName(String userName);

    // 更新使用者資料 (例如改密碼或名稱)
    UserResponse updateUser(String userId, UserUpdateRequest request);

    // 刪除帳號
    void deleteUser(String userId);
}
