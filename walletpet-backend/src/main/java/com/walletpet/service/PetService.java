package com.walletpet.service;

import com.walletpet.dto.pet.PetResponse;
import com.walletpet.entity.Pet;
import com.walletpet.entity.User;

public interface PetService {

    PetResponse getMyPet(String currentUserId);

    PetResponse feedPet(String currentUserId, String foodType);

    PetResponse increaseCancan(
            String currentUserId,
            Integer amount,
            String reason
    );

    PetResponse changeMood(
            String currentUserId,
            Integer moodDelta,
            String eventType,
            String reward
    );

    void createDefaultPetForUser(
            User user,
            String petName
    );

    Pet getDisplayedPetEntity(
            String currentUserId
    );
}