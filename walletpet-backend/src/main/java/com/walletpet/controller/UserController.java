package com.walletpet.controller;

import com.walletpet.dto.common.ApiResponse;
import com.walletpet.dto.user.UserRegisterRequest;
import com.walletpet.dto.user.UserResponse;
import com.walletpet.dto.user.UserUpdateRequest;
import com.walletpet.security.CurrentUserUtil;
import com.walletpet.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final CurrentUserUtil currentUserUtil;

    // 註冊：POST http://localhost:8080/walletpet/api/users/register
    @PostMapping("/register")
    public ApiResponse<UserResponse> register(@RequestBody UserRegisterRequest request) {
        UserResponse response = userService.registerUser(request);
        return ApiResponse.success("註冊成功", response);
    }

    // 取得目前登入者資料：GET http://localhost:8080/walletpet/api/users/me
    @GetMapping("/me")
    public ApiResponse<UserResponse> getMe() {
        String currentUserId = currentUserUtil.getCurrentUserId();
        UserResponse response = userService.findUserById(currentUserId);
        return ApiResponse.success("查詢成功", response);
    }

    // 修改目前登入者資料：PUT http://localhost:8080/walletpet/api/users/me
    @PutMapping("/me")
    public ApiResponse<UserResponse> updateMe(@RequestBody UserUpdateRequest request) {
        String currentUserId = currentUserUtil.getCurrentUserId();
        UserResponse response = userService.updateUser(currentUserId, request);
        return ApiResponse.success("修改成功", response);
    }
}