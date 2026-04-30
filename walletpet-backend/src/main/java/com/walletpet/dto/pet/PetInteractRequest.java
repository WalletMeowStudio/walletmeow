package com.walletpet.dto.pet;

import com.walletpet.enums.PetInteractionType;

import lombok.Data;

@Data
public class PetInteractRequest {

    private PetInteractionType interactionType;
}