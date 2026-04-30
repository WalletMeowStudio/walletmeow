package com.walletpet.dto.pet;

import lombok.Data;

@Data
public class PetEventCreateRequest {
    private String userId;
    private String petId;
    private String eventType;
    private Integer moodDelta;
    private Integer cancanDelta;
    private String reward;
}