package com.walletpet.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.walletpet.dto.dailyreward.DailyRewardResponse;
import com.walletpet.entity.DailyRecordReward;
import com.walletpet.entity.User;
import com.walletpet.enums.RewardType;
import com.walletpet.enums.TransactionType;
import com.walletpet.exception.ResourceNotFoundException;
import com.walletpet.repository.DailyRecordRewardRepository;
import com.walletpet.repository.TransactionRepository;
import com.walletpet.repository.UserRepository;
import com.walletpet.service.DailyRewardService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class DailyRewardServiceImpl implements DailyRewardService {

	private final DailyRecordRewardRepository dailyRecordRewardRepository;
	private final UserRepository userRepository;
	private final TransactionRepository transactionRepository;

    /**
     * 交易新增、修改、刪除後，重新計算某一天的每日記帳獎勵。
     *
     * 邏輯：
     * 1. 計算當天收入 / 支出交易筆數
     * 2. 若筆數 > 0，qualified = true
     * 3. 依照前一天 streak_days 推算今日 streak_days
     * 4. 依 streak_days 決定 reward_type、reward_value、mood_delta
     * 5. 若尚未發放，設定 claimed_at
     */
    @Override
    public DailyRewardResponse handleDailyReward(
            String currentUserId,
            LocalDate rewardDate
    ) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到該使用者"));

        DailyRecordReward reward = dailyRecordRewardRepository
                .findByUser_UserIdAndRewardDate(currentUserId, rewardDate)
                .orElseGet(() -> {
                    DailyRecordReward newReward = new DailyRecordReward();
                    newReward.setUser(user);
                    newReward.setRewardDate(rewardDate);
                    return newReward;
                });

        int transactionCount = transactionRepository
                .countByUser_UserIdAndTransactionDateAndTransactionTypeIn(
                        currentUserId,
                        rewardDate,
                        Arrays.asList(TransactionType.INCOME, TransactionType.EXPENSE)
                );

        reward.setTransactionCount(transactionCount);

        if (transactionCount <= 0) {
            reward.setQualified(false);
            reward.setStreakDays(0);
            reward.setRewardType(null);
            reward.setRewardValue(null);
            reward.setMoodDelta(0);
            reward.setClaimedAt(null);

            DailyRecordReward savedReward = dailyRecordRewardRepository.save(reward);
            return toResponse(savedReward);
        }

        reward.setQualified(true);

        int streakDays = calculateStreakDays(currentUserId, rewardDate);
        reward.setStreakDays(streakDays);

        applyRewardRule(reward, streakDays);

        if (reward.getClaimedAt() == null) {
            reward.setClaimedAt(LocalDateTime.now());
        }

        DailyRecordReward savedReward = dailyRecordRewardRepository.save(reward);

        return toResponse(savedReward);
    }

    /**
     * 查詢某一天的每日獎勵。
     */
    @Override
    @Transactional(readOnly = true)
    public DailyRewardResponse getRewardByDate(
            String currentUserId,
            LocalDate rewardDate
    ) {
        return dailyRecordRewardRepository
                .findByUser_UserIdAndRewardDate(currentUserId, rewardDate)
                .map(this::toResponse)
                .orElse(null);
    }

    /**
     * 查詢最近 30 筆每日獎勵紀錄。
     */
    @Override
    @Transactional(readOnly = true)
    public List<DailyRewardResponse> getHistory(
            String currentUserId
    ) {
        return dailyRecordRewardRepository
                .findTop30ByUser_UserIdOrderByRewardDateDesc(currentUserId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 計算連續記帳天數。
     *
     * 簡化邏輯：
     * 只看前一天 daily_record_rewards 是否 qualified=true。
     *
     * 若昨天有 qualified，今天 streak = 昨天 streak + 1。
     * 若昨天沒有紀錄或 qualified=false，今天 streak = 1。
     */
    private int calculateStreakDays(
            String currentUserId,
            LocalDate rewardDate
    ) {
        LocalDate previousDate = rewardDate.minusDays(1);

        DailyRecordReward yesterdayReward = dailyRecordRewardRepository
                .findByUser_UserIdAndRewardDate(currentUserId, previousDate)
                .orElse(null);

        if (yesterdayReward != null && Boolean.TRUE.equals(yesterdayReward.getQualified())) {
            return yesterdayReward.getStreakDays() + 1;
        }

        return 1;
    }

    /**
     * 依照連續記帳天數套用獎勵規則。
     *
     * 目前規則：
     * 1 ~ 2 天：MOOD_BONUS，心情 +5
     * 3 ~ 6 天：CAT_FOOD，貓食 +1，心情 +7
     * 7 ~ 29 天：SPECIAL_ANIMATION，特殊動畫 +1，心情 +10
     * 30 天以上：BADGE，徽章 +1，心情 +20
     */
    private void applyRewardRule(DailyRecordReward reward,int streakDays) {
        if (streakDays >= 30) {
            reward.setRewardType(RewardType.BADGE);
            reward.setRewardValue(1);
            reward.setMoodDelta(20);
        } else if (streakDays >= 7) {
            reward.setRewardType(RewardType.SPECIAL_ANIMATION);
            reward.setRewardValue(1);
            reward.setMoodDelta(10);
        } else if (streakDays >= 3) {
            reward.setRewardType(RewardType.CAT_FOOD);
            reward.setRewardValue(1);
            reward.setMoodDelta(7);
        } else {
            reward.setRewardType(RewardType.MOOD_BONUS);
            reward.setRewardValue(5);
            reward.setMoodDelta(5);
        }
    }

    /**
     * Entity 轉 DTO。
     */
    private DailyRewardResponse toResponse(DailyRecordReward reward) {
        DailyRewardResponse response = new DailyRewardResponse();

        response.setRewardDate(reward.getRewardDate());
        response.setQualified(reward.getQualified());
        response.setTransactionCount(reward.getTransactionCount());
        response.setStreakDays(reward.getStreakDays());
        response.setRewardType(reward.getRewardType());
        response.setRewardValue(reward.getRewardValue());
        response.setMoodDelta(reward.getMoodDelta());
        response.setClaimedAt(reward.getClaimedAt());

        return response;
    }
}