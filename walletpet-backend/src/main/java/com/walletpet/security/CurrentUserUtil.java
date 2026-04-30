package com.walletpet.security;

import com.walletpet.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrentUserUtil {

    private final HttpServletRequest request;
    private final TokenService tokenService;

    public String getCurrentUserId() {
        String authorization = request.getHeader("Authorization");

        if (authorization == null || authorization.isBlank()) {
            throw new UnauthorizedException("請先登入");
        }

        if (!authorization.startsWith("Bearer ")) {
            throw new UnauthorizedException("Authorization 格式錯誤");
        }

        String token = authorization.substring(7);

        if (token.isBlank()) {
            throw new UnauthorizedException("token 不可為空");
        }

        return tokenService.getUserIdByToken(token);
    }
}