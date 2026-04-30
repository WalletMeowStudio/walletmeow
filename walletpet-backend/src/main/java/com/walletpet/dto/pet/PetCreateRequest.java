package com.walletpet.dto.pet;

import lombok.Data;

@Data
public class PetCreateRequest {
    private String petId;
    private String userId;
    private String petName;
    private Integer mood;
    private Integer cancan;
    private Boolean isDisplayed;
}