package com.walletpet.service;

import java.time.LocalDate;
import java.util.List;

import com.walletpet.dto.dailyreward.DailyRewardResponse;

public interface DailyRewardService {

    /**
     * 交易新增、修改、刪除後，重新計算某使用者某一天的每日記帳獎勵。
     */
    DailyRewardResponse handleDailyReward(String currentUserId,LocalDate rewardDate);

    /**
     * 查詢某一天的每日獎勵。
     */
    DailyRewardResponse getRewardByDate(String currentUserId,LocalDate rewardDate);

    /**
     * 查詢最近 30 筆每日獎勵紀錄。
     */
    List<DailyRewardResponse> getHistory(String currentUserId);
}