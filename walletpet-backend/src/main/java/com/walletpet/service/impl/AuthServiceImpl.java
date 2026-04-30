package com.walletpet.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.walletpet.dto.auth.LoginRequest;
import com.walletpet.dto.auth.LoginResponse;
import com.walletpet.entity.User;
import com.walletpet.exception.BusinessException;
import com.walletpet.repository.UserRepository;
import com.walletpet.security.TokenService;
import com.walletpet.service.AuthService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final TokenService tokenService;

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {

        if (request == null) {
            throw new BusinessException("登入資料不可為空");
        }

        if (request.getUserName() == null || request.getUserName().isBlank()) {
            throw new BusinessException("帳號不可為空");
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BusinessException("密碼不可為空");
        }

        User user = userRepository.findByUserName(request.getUserName())
                .orElseThrow(() -> new BusinessException("帳號或密碼錯誤"));

        if (!request.getPassword().equals(user.getPassword())) {
            throw new BusinessException("帳號或密碼錯誤");
        }

        String token = tokenService.createToken(user.getUserId());

        return new LoginResponse(
                user.getUserId(),
                user.getUserName(),
                user.getRole(),
                token
        );
    }
}