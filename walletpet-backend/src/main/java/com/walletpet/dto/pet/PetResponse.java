package com.walletpet.dto.pet;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PetResponse {
    private String petId;

    private String petName;

    private Integer mood;
<<<<<<< HEAD
    private Integer cancan;
    private LocalDateTime lastUpdateAt;
=======
    
    private Integer cancan;

    private Integer modelId;

    private String riveName;

>>>>>>> tzuchen
    private Boolean isDisplayed;
    
    private LocalDateTime lastUpdateAt;
}