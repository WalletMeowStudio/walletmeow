package com.walletpet.dto.pet;

import lombok.Data;

@Data
public class PetInteractResponse {

    private String petId;

    private Integer mood;

    private Integer food;

    private String eventType;

    private String animation;
}