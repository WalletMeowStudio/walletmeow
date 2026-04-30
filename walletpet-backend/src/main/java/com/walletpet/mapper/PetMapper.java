package com.walletpet.mapper;

import com.walletpet.dto.pet.PetResponse;
import com.walletpet.entity.Pet;

public class PetMapper {

    private PetMapper() {

    }

    public static PetResponse toResponse(Pet pet) {
        if (pet == null) {
            return null;
        }

        PetResponse response = new PetResponse();

        response.setPetId(pet.getPetId());
        response.setPetName(pet.getPetName());
        response.setMood(pet.getMood());
        response.setCancan(pet.getCancan());
<<<<<<< HEAD
        response.setLastUpdateAt(pet.getLastUpdateAt());
=======
>>>>>>> tzuchen
        response.setIsDisplayed(pet.getIsDisplayed());
        response.setLastUpdateAt(pet.getLastUpdateAt());

        if (pet.getModel() != null) {
            response.setModelId(pet.getModel().getPetModelId());
            response.setRiveName(pet.getModel().getRiveName());
        }

        return response;
    }
}