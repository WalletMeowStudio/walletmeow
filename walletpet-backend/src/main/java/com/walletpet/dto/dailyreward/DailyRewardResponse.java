package com.walletpet.dto.dailyreward;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class DailyRewardResponse {

    private Long dailyRewardId;

    private LocalDate rewardDate;

    private Boolean qualified;

    private Integer transactionCount;

    private Integer streakDays;

    private String rewardType;

    private Integer rewardValue;

    private Integer moodDelta;

    private Integer cancanDelta;

    private LocalDateTime claimedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}