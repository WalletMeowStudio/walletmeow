package com.walletpet.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.walletpet.dto.pet.PetEventResponse;
import com.walletpet.entity.Pet;
import com.walletpet.entity.PetEvent;
import com.walletpet.entity.User;
import com.walletpet.exception.BusinessException;
import com.walletpet.mapper.PetEventMapper;
import com.walletpet.repository.PetEventRepository;
import com.walletpet.service.PetEventService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PetEventServiceImpl implements PetEventService {

    private final PetEventRepository petEventRepository;

    /*
     * 統一建立寵物事件。
     *
     * 所有會造成 mood / cancan 變動的流程都應該呼叫這個方法：
     * 1. 餵食
     * 2. 每日記帳獎勵
     * 3. 登入 streak
     * 4. 管理員手動調整 mood
     *
     * 注意：
     * PetEvent 只是事件流水帳。
     * 真正的寵物目前狀態仍以 pets 表為準。
     */
    @Override
    public PetEvent createEvent(
            User user,
            Pet pet,
            String eventType,
            Integer moodDelta,
            Integer cancanDelta,
            String reward
    ) {
        if (user == null || !hasText(user.getUserId())) {
            throw new BusinessException("寵物事件缺少使用者資料");
        }

        if (pet == null || !hasText(pet.getPetId())) {
            throw new BusinessException("寵物事件缺少寵物資料");
        }

        if (!hasText(eventType)) {
            throw new BusinessException("寵物事件類型不可為空");
        }

        PetEvent petEvent = new PetEvent();
        petEvent.setUser(user);
        petEvent.setPet(pet);
        petEvent.setEventType(eventType);
        petEvent.setMoodDelta(moodDelta == null ? 0 : moodDelta);
        petEvent.setCancanDelta(cancanDelta == null ? 0 : cancanDelta);
        petEvent.setReward(normalizeNullableText(reward));
        petEvent.setCreatedAt(LocalDateTime.now());

        return petEventRepository.save(petEvent);
    }

    /*
     * 查詢目前登入者的寵物事件。
     *
     * DTO 最小化原則：
     * 1. 不建立 PetEventPageResponse。
     * 2. 外層分頁資料用 Map<String, Object>。
     * 3. items 內仍使用 PetEventResponse，避免直接回傳 Entity。
     * 4. PetEventResponse 不回傳 userId / petId。
     */
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getMyPetEvents(
            String currentUserId,
            int page,
            int size
    ) {
        validateCurrentUserId(currentUserId);
        validatePageParameter(page, size);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Order.desc("createdAt"))
        );

        Page<PetEvent> petEventPage = petEventRepository
                .findByUser_UserIdOrderByCreatedAtDesc(currentUserId, pageable);

        List<PetEventResponse> items = petEventPage.getContent()
                .stream()
                .map(PetEventMapper::toResponse)
                .collect(Collectors.toList());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("items", items);
        response.put("page", petEventPage.getNumber());
        response.put("size", petEventPage.getSize());
        response.put("totalElements", petEventPage.getTotalElements());
        response.put("totalPages", petEventPage.getTotalPages());
        response.put("first", petEventPage.isFirst());
        response.put("last", petEventPage.isLast());

        return response;
    }

    /*
     * 加總某使用者某一天一般餵食造成的 mood 增加量。
     *
     * 用途：
     * CAN / FISH / SNACK 一般餵食每日 mood 增加上限為 +3。
     * FEAST 不受此限制，所以這裡不納入 PET_FEED_FEAST。
     */
    @Override
    @Transactional(readOnly = true)
    public int sumTodayNormalFeedMoodGain(
            String currentUserId,
            LocalDate date
    ) {
        validateCurrentUserId(currentUserId);

        if (date == null) {
            throw new BusinessException("日期不可為空");
        }

        LocalDateTime startDateTime = date.atStartOfDay();
        LocalDateTime endDateTime = date.plusDays(1).atStartOfDay();
        
        List<String> normalFeedEventTypes = Arrays.asList(
                "PET_FEED_CAN",
                "PET_FEED_FISH",
                "PET_FEED_SNACK"
        );

        int totalMoodDelta = petEventRepository.sumMoodDeltaByUserAndTypeAndPeriod(
                currentUserId,
                normalFeedEventTypes,
                startDateTime,
                endDateTime
        );

        return totalMoodDelta;
    }

    private void validateCurrentUserId(String currentUserId) {
        if (!hasText(currentUserId)) {
            throw new BusinessException("使用者不可為空");
        }
    }

    private void validatePageParameter(
            int page,
            int size
    ) {
        if (page < 0) {
            throw new BusinessException("頁碼不可小於 0");
        }

        if (size <= 0) {
            throw new BusinessException("每頁筆數必須大於 0");
        }

        if (size > 100) {
            throw new BusinessException("每頁筆數不可超過 100");
        }
    }

    private String normalizeNullableText(String value) {
        if (value == null) {
            return null;
        }

        String trimmedValue = value.trim();

        if (trimmedValue.isEmpty()) {
            return null;
        }

        return trimmedValue;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}