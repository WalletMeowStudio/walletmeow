package com.walletpet.dto.pet;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PetResponse {
    private String petId;

    private String petName;

    private Integer mood;
    
    private Integer cancan;

    private Integer modelId;

    private String riveName;

    private Boolean isDisplayed;
    
    private LocalDateTime lastUpdateAt;
}