package com.walletpet.service;

import com.walletpet.dto.auth.LoginRequest;
import com.walletpet.dto.auth.LoginResponse;

public interface AuthService {
	LoginResponse login(LoginRequest request);
}
