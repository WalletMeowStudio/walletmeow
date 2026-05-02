package com.walletpet.service;

import java.time.LocalDate;
import java.util.Map;

import com.walletpet.entity.Pet;
import com.walletpet.entity.PetEvent;
import com.walletpet.entity.User;

public interface PetEventService {

    PetEvent createEvent(
            User user,
            Pet pet,
            String eventType,
            Integer moodDelta,
            Integer cancanDelta,
            String reward
    );

    Map<String, Object> getMyPetEvents(
            String currentUserId,
            int page,
            int size
    );

    int sumTodayNormalFeedMoodGain(
            String currentUserId,
            LocalDate date
    );
}