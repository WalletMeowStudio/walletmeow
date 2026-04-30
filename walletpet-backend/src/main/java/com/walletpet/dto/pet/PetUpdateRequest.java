package com.walletpet.dto.pet;

import lombok.Data;

@Data
public class PetUpdateRequest {
    private String petName;
    private Integer mood;
    private Integer cancan;
    private Boolean isDisplayed;
}