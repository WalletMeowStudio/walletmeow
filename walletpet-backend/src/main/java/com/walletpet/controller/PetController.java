package com.walletpet.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.walletpet.dto.common.ApiResponse;
import com.walletpet.dto.pet.LoginTickResponse;
import com.walletpet.dto.pet.PetResponse;
import com.walletpet.security.CurrentUserUtil;
import com.walletpet.service.LoginStreakService;
import com.walletpet.service.PetService;

import lombok.RequiredArgsConstructor;
@CrossOrigin(origins = "http://127.0.0.1:5500", allowedHeaders = "*", allowCredentials = "true")
@RestController
@RequestMapping("/api/pets")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;

    private final LoginStreakService loginStreakService;

    private final CurrentUserUtil currentUserUtil;

    /*
     * 查詢目前登入者顯示中的寵物
     *
     * GET /walletpet/api/pets/me
     */
    @GetMapping("/me")
    public ApiResponse<PetResponse> getMyPet() {
        String currentUserId = currentUserUtil.getCurrentUserId();

        PetResponse data = petService.getMyPet(currentUserId);

        return ApiResponse.success("查詢成功", data);
    }

    /*
     * 餵食目前登入者的寵物
     *
     * POST /walletpet/api/pets/feed?foodType=CAN
     */
    @PostMapping("/feed")
    public ApiResponse<PetResponse> feedPet(
            @RequestParam String foodType
    ) {
        String currentUserId = currentUserUtil.getCurrentUserId();

        PetResponse data = petService.feedPet(
                currentUserId,
                foodType
        );

        return ApiResponse.success("餵食成功", data);
    }

    /*
     * 登入 tick
     *
     * POST /walletpet/api/pets/login-tick
     * POST /walletpet/api/pets/login-tick?loginDate=2026-04-29
     *
     * 正式環境可不傳 loginDate，後端預設今天。
     * 測試 streak 時可暫時傳 loginDate。
     */
    @PostMapping("/login-tick")
    public ApiResponse<LoginTickResponse> loginTick(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate loginDate
    ) {
        String currentUserId = currentUserUtil.getCurrentUserId();

        LoginTickResponse data = loginStreakService.loginTick(
                currentUserId,
                loginDate
        );

        return ApiResponse.success("登入紀錄完成", data);
    }
}