package com.walletpet.service;

import java.time.LocalDate;

import com.walletpet.dto.pet.LoginTickResponse;

public interface LoginStreakService {

    LoginTickResponse loginTick(
            String currentUserId,
            LocalDate loginDate
    );
}