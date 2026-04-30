package com.walletpet.mapper;

import com.walletpet.dto.pet.PetEventResponse;
import com.walletpet.entity.PetEvent;

public class PetEventMapper {

    private PetEventMapper() {

    }

    public static PetEventResponse toResponse(PetEvent petEvent) {
        if (petEvent == null) {
            return null;
        }

        PetEventResponse response = new PetEventResponse();

        response.setPetEventId(petEvent.getPetEventId());

        if (petEvent.getPet() != null) {
            response.setPetName(petEvent.getPet().getPetName());
        }

        response.setEventType(petEvent.getEventType());
        response.setMoodDelta(petEvent.getMoodDelta());
        response.setCancanDelta(petEvent.getCancanDelta());
        response.setReward(petEvent.getReward());
        response.setCreatedAt(petEvent.getCreatedAt());

        return response;
    }
}