package com.walletpet.mapper;

import com.walletpet.dto.pet.PetEventResponse;
import com.walletpet.entity.PetEvent;

public class PetEventMapper {

    private PetEventMapper() {
    }

    public static PetEventResponse toResponse(PetEvent event) {
        if (event == null) {
            return null;
        }

        PetEventResponse response = new PetEventResponse();

        response.setPetEventId(event.getPetEventId());

        if (event.getPet() != null) {
            response.setPetId(event.getPet().getPetId());
            response.setPetName(event.getPet().getPetName());
        }

        response.setEventType(event.getEventType());
        response.setMoodDelta(event.getMoodDelta());
<<<<<<< HEAD
        response.setCancanDelta(event.getCancanDelta());
=======
>>>>>>> tzuchen
        response.setReward(event.getReward());
        response.setCreatedAt(event.getCreatedAt());

        return response;
    }
}