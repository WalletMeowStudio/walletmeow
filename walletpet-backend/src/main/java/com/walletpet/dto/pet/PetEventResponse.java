package com.walletpet.dto.pet;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PetEventResponse {
    private Long petEventId;
    private String userId;
    private String petId;
    private String petName;
    private String eventType;
    private Integer moodDelta;
    private Integer cancanDelta;
    private String reward;
    private LocalDateTime createdAt;
}