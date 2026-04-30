package com.walletpet.dto.dailyreward;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.walletpet.enums.RewardType;

import lombok.Data;

@Data
public class DailyRewardResponse {

    private LocalDate rewardDate;

    private Boolean qualified;

    private Integer transactionCount;

    private Integer streakDays;

    private RewardType rewardType;

    private Integer rewardValue;

    private Integer moodDelta;

    private LocalDateTime claimedAt;
}