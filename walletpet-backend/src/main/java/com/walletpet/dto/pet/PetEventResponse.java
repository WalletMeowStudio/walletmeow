package com.walletpet.dto.pet;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class PetEventResponse {

    private Long petEventId;

    private String petName;

    private String eventType;

    private Integer moodDelta;

    private Integer cancanDelta;

    private String reward;

    private LocalDateTime createdAt;
}