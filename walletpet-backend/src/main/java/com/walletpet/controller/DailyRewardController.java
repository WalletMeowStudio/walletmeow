package com.walletpet.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.walletpet.dto.common.ApiResponse;
import com.walletpet.dto.dailyreward.DailyRewardResponse;
import com.walletpet.security.CurrentUserUtil;
import com.walletpet.service.DailyRewardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/rewards/daily")
@RequiredArgsConstructor
public class DailyRewardController {

    private final DailyRewardService dailyRewardService;

    private final CurrentUserUtil currentUserUtil;

    /**
     * 手動重新計算某一天的每日記帳獎勵。
     *
     * 測試用：
     * POST http://localhost:8080/walletpet/api/rewards/daily/calculate?date=2026-04-25
     */
    @PostMapping("/calculate")
    public ApiResponse<DailyRewardResponse> calculateReward(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        String currentUserId = currentUserUtil.getCurrentUserId();

        DailyRewardResponse data = dailyRewardService.handleDailyReward(
                currentUserId,
                date
        );

        return ApiResponse.success("每日記帳獎勵計算成功", data);
    }

    /**
     * 查詢指定日期的每日獎勵。
     *
     * GET http://localhost:8080/walletpet/api/rewards/daily/today?date=2026-04-25
     */
    @GetMapping("/today")
    public ApiResponse<DailyRewardResponse> getTodayReward(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        String currentUserId = currentUserUtil.getCurrentUserId();

        DailyRewardResponse data = dailyRewardService.getRewardByDate(
                currentUserId,
                date
        );

        return ApiResponse.success("查詢成功", data);
    }

    /**
     * 查詢最近 30 筆每日獎勵紀錄。
     *
     * GET http://localhost:8080/walletpet/api/rewards/daily/history
     */
    @GetMapping("/history")
    public ApiResponse<List<DailyRewardResponse>> getHistory() {
        String currentUserId = currentUserUtil.getCurrentUserId();

        List<DailyRewardResponse> data = dailyRewardService.getHistory(
                currentUserId
        );

        return ApiResponse.success("查詢成功", data);
    }
}