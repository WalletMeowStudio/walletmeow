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

    /*
     * 手動重新計算指定日期每日記帳獎勵。
     *
     * 主要供測試或交易異常後重算使用。
     *
     * POST /walletpet/api/rewards/daily/calculate?date=2026-04-29
     *
     * 注意：
     * 正式交易流程會由 TransactionService 自動呼叫 DailyRewardService。
     * 這支 API 不是主要發獎入口。
     */
    @PostMapping("/calculate")
    public ApiResponse<DailyRewardResponse> calculateDailyReward(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        String currentUserId = currentUserUtil.getCurrentUserId();

        DailyRewardResponse data = dailyRewardService.calculateDailyReward(
                currentUserId,
                date
        );

        return ApiResponse.success("每日記帳獎勵計算成功", data);
    }

    /*
     * 查詢今日或指定日期每日記帳獎勵狀態。
     *
     * GET /walletpet/api/rewards/daily/today
     * GET /walletpet/api/rewards/daily/today?date=2026-04-29
     */
    @GetMapping("/today")
    public ApiResponse<DailyRewardResponse> getTodayReward(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        String currentUserId = currentUserUtil.getCurrentUserId();

        DailyRewardResponse data = dailyRewardService.getTodayReward(
                currentUserId,
                date
        );

        return ApiResponse.success("查詢成功", data);
    }

    /*
     * 查詢目前登入者的每日記帳獎勵歷史。
     *
     * GET /walletpet/api/rewards/daily/history
     */
    @GetMapping("/history")
    public ApiResponse<List<DailyRewardResponse>> getRewardHistory() {
        String currentUserId = currentUserUtil.getCurrentUserId();

        List<DailyRewardResponse> data = dailyRewardService.getRewardHistory(
                currentUserId
        );

        return ApiResponse.success("查詢成功", data);
    }
}