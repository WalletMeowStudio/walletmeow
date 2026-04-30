package com.walletpet.service;

import java.time.LocalDate;
import java.util.List;

import com.walletpet.dto.dailyreward.DailyRewardResponse;

public interface DailyRewardService {

    /*
     * TransactionService 新增 / 修改 / 刪除交易後呼叫。
     *
     * 主要責任：
     * 1. 計算指定日期的收入 / 支出交易筆數
     * 2. 更新 daily_record_rewards
     * 3. 若符合條件且未達每日 cancan +5 上限，增加 pets.cancan
     * 4. 寫入 pet_events
     */
    DailyRewardResponse handleDailyReward(
            String currentUserId,
            LocalDate rewardDate
    );

    /*
     * 手動重新計算指定日期。
     * 可供 Controller 測試用。
     */
    DailyRewardResponse calculateDailyReward(
            String currentUserId,
            LocalDate rewardDate
    );

    /*
     * 查詢今日或指定日期的每日記帳獎勵狀態。
     */
    DailyRewardResponse getTodayReward(
            String currentUserId,
            LocalDate rewardDate
    );

    /*
     * 查詢每日記帳獎勵歷史。
     */
    List<DailyRewardResponse> getRewardHistory(
            String currentUserId
    );
}