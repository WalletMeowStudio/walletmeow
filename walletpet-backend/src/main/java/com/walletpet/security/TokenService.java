package com.walletpet.security;

import com.walletpet.exception.UnauthorizedException;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenService {

    private final Map<String, String> tokenUserMap = new ConcurrentHashMap<>();

    public String createToken(String userId) {
        String token = UUID.randomUUID().toString();
        tokenUserMap.put(token, userId);
        return token;
    }

    public String getUserIdByToken(String token) {
        String userId = tokenUserMap.get(token);

        if (userId == null) {
            throw new UnauthorizedException("登入已失效，請重新登入");
        }

        return userId;
    }

    public void removeToken(String token) {
        tokenUserMap.remove(token);
    }
}