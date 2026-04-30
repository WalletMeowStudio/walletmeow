package com.walletpet.dto.pet;

import com.walletpet.enums.RewardType;

import lombok.Data;

@Data
public class PetRewardResponse {

    private String petId;

    private Integer mood;

    private Integer moodDelta;

    private Integer food;

    private RewardType rewardType;

    private Integer rewardValue;

    private String reward;

    private String eventType;
}