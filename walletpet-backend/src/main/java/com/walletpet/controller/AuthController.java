package com.walletpet.controller;

import org.springframework.web.bind.annotation.*;

import com.walletpet.dto.auth.LoginRequest;
import com.walletpet.dto.auth.LoginResponse;
import com.walletpet.dto.common.ApiResponse;
import com.walletpet.service.AuthService;

import lombok.RequiredArgsConstructor;
@CrossOrigin(origins = "http://127.0.0.1:5500", allowedHeaders = "*", allowCredentials = "true")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        return ApiResponse.success("登入成功", authService.login(request));
    }
}