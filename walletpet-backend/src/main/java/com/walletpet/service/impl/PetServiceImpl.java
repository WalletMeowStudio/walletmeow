package com.walletpet.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.walletpet.dto.pet.PetResponse;
import com.walletpet.entity.Pet;
import com.walletpet.entity.PetModel;
import com.walletpet.entity.User;
import com.walletpet.exception.BusinessException;
import com.walletpet.exception.ResourceNotFoundException;
import com.walletpet.mapper.PetMapper;
import com.walletpet.repository.PetModelRepository;
import com.walletpet.repository.PetRepository;
import com.walletpet.service.PetEventService;
import com.walletpet.service.PetService;
import com.walletpet.util.IdGenerator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PetServiceImpl implements PetService {

    /* ========== 規則常數（小組會議定的數值，集中在這） ==========
     * mood 區間 0~100；預設值 60（首次建立寵物 / 自動回歸用）
     * cancan 下限 0、無上限
     */
    private static final int MOOD_MIN = 0;
    private static final int MOOD_MAX = 100;
    private static final int MOOD_DEFAULT = 60;

    /* 餵食基本動作 */
    private static final int BASIC_FEED_COST = 1;
    private static final int BASIC_FEED_MOOD_GAIN = 1;

    /* 大餐 */
    private static final int FEAST_COST = 10;
    private static final int FEAST_MOOD_GAIN = 15;

    /* 每日上限 */
    private static final int DAILY_FEED_MOOD_CAP = 3; // 每日 +3 mood 上限（餵罐罐 / 小魚乾 / 零食 合計）

    /* event_type 約定值（與前端 EVENT_META 對齊） */
    private static final String EVT_FEED_CAN = "PET_FEED_CAN";
    private static final String EVT_FEED_FISH = "PET_FEED_FISH";
    private static final String EVT_FEED_SNACK = "PET_FEED_SNACK";
    private static final String EVT_FEED_FEAST = "PET_FEED_FEAST";
    private static final String EVT_CANCAN_ADJUST = "DAILY_BOOKKEEPING_REWARD";

    private final PetRepository petRepository;

    private final PetModelRepository petModelRepository;

    private final PetEventService petEventService;

    /*
     * 查詢目前登入者顯示中的寵物。
     *
     * 一般使用者不需要傳 petId。
     * 後端會用 currentUserId 找 isDisplayed = true 的寵物。
     */
    @Override
    @Transactional(readOnly = true)
    public PetResponse getMyPet(String currentUserId) {
        Pet pet = getDisplayedPetEntity(currentUserId);
        return PetMapper.toResponse(pet);
    }

    /*
     * 取得目前顯示寵物 Entity。
     *
     * 給 DailyRewardService / LoginStreakService 內部使用。
     * 外部 API 回傳仍應使用 PetResponse。
     */
    @Override
    @Transactional(readOnly = true)
    public Pet getDisplayedPetEntity(String currentUserId) {
        validateCurrentUserId(currentUserId);

        return petRepository.findFirstByUser_UserIdAndIsDisplayedTrue(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到目前顯示的寵物"));
    }

    /*
     * 餵食。
     *
     * 前端只需要傳 foodType：
     * CAN / FISH / SNACK / FEAST
     *
     * 不需要傳 userId。
     * 不需要傳 petId。
     */
    @Override
    public PetResponse feedPet(String currentUserId, String foodType) {
        validateCurrentUserId(currentUserId);

        if (!hasText(foodType)) {
            throw new BusinessException("foodType 不可為空");
        }

        Pet pet = getDisplayedPetEntity(currentUserId);
        User user = pet.getUser();

        String normalizedFoodType = foodType.trim().toUpperCase();

        switch (normalizedFoodType) {
            case "CAN":
                return doBasicFeed(user, pet, EVT_FEED_CAN, "餵食 CAN：食物量 cancan -1，心情值 mood +1");
            case "FISH":
                return doBasicFeed(user, pet, EVT_FEED_FISH, "餵食 FISH：食物量 cancan -1，心情值 mood +1");
            case "SNACK":
                return doBasicFeed(user, pet, EVT_FEED_SNACK, "餵食 SNACK：食物量 cancan -1，心情值 mood +1");
            case "FEAST":
                return doFeastFeed(user, pet);
            default:
                throw new BusinessException("未知 foodType：" + foodType);
        }
    }

    /*
     * 一般餵食：
     * CAN / FISH / SNACK
     *
     * 食物量 cancan -1。
     * 心情值 mood +1。
     * 一般餵食每日 mood 增加上限 +3。
     */
    private PetResponse doBasicFeed(
            User user,
            Pet pet,
            String eventType,
            String reward
    ) {
        if (safeInt(pet.getCancan()) < BASIC_FEED_COST) {
            throw new BusinessException("食物量 cancan 不足，無法餵食");
        }

        int gainedToday = petEventService.sumTodayNormalFeedMoodGain(
                user.getUserId(),
                LocalDate.now()
        );

        int allowedMoodGain = Math.max(
                0,
                DAILY_FEED_MOOD_CAP - gainedToday
        );

        int moodDelta = Math.min(
                BASIC_FEED_MOOD_GAIN,
                allowedMoodGain
        );

        int cancanDelta = -BASIC_FEED_COST;

        applyDeltas(pet, moodDelta, cancanDelta);

        petEventService.createEvent(
                user,
                pet,
                eventType,
                moodDelta,
                cancanDelta,
                reward
        );

        Pet savedPet = petRepository.save(pet);

        return PetMapper.toResponse(savedPet);
    }

    /*
     * 大餐：
     * FEAST
     *
     * 食物量 cancan -10。
     * 心情值 mood +15。
     * 不受每日 mood +3 上限限制。
     */
    private PetResponse doFeastFeed(
            User user,
            Pet pet
    ) {
        if (safeInt(pet.getCancan()) < FEAST_COST) {
            throw new BusinessException("食物量 cancan 不足，大餐需要 " + FEAST_COST + " cancan");
        }

        int moodDelta = FEAST_MOOD_GAIN;
        int cancanDelta = -FEAST_COST;

        applyDeltas(pet, moodDelta, cancanDelta);

        petEventService.createEvent(
                user,
                pet,
                EVT_FEED_FEAST,
                moodDelta,
                cancanDelta,
                "餵食 FEAST：食物量 cancan -10，心情值 mood +15"
        );

        Pet savedPet = petRepository.save(pet);

        return PetMapper.toResponse(savedPet);
    }

    /*
     * 增加食物量 cancan。
     *
     * 主要給 DailyRewardService 使用。
     * 若 DailyRewardService 已經直接操作 PetRepository，
     * 這個方法仍可保留作為未來統一入口。
     */
    @Override
    public PetResponse increaseCancan(
            String currentUserId,
            Integer amount,
            String reason
    ) {
        validateCurrentUserId(currentUserId);

        if (amount == null || amount <= 0) {
            throw new BusinessException("增加的 cancan 數量必須大於 0");
        }

        Pet pet = getDisplayedPetEntity(currentUserId);
        User user = pet.getUser();

        applyDeltas(pet, 0, amount);

        petEventService.createEvent(
                user,
                pet,
                EVT_CANCAN_ADJUST,
                0,
                amount,
                reason == null ? "食物量 cancan +" + amount : reason
        );

        Pet savedPet = petRepository.save(pet);

        return PetMapper.toResponse(savedPet);
    }

    /*
     * 調整心情值 mood。
     *
     * 主要給 LoginStreakService 或 AdminPetTestService 使用。
     */
    @Override
    public PetResponse changeMood(
            String currentUserId,
            Integer moodDelta,
            String eventType,
            String reward
    ) {
        validateCurrentUserId(currentUserId);

        if (moodDelta == null || moodDelta == 0) {
            return getMyPet(currentUserId);
        }

        if (!hasText(eventType)) {
            throw new BusinessException("eventType 不可為空");
        }

        Pet pet = getDisplayedPetEntity(currentUserId);
        User user = pet.getUser();

        applyDeltas(pet, moodDelta, 0);

        petEventService.createEvent(
                user,
                pet,
                eventType,
                moodDelta,
                0,
                reward
        );

        Pet savedPet = petRepository.save(pet);

        return PetMapper.toResponse(savedPet);
    }

    /*
     * 建立新使用者的預設寵物。
     *
     * 註冊流程呼叫。
     * modelId 目前固定為 1。
     */
    @Override
    public void createDefaultPetForUser(
            User user,
            String petName
    ) {
        if (user == null || !hasText(user.getUserId())) {
            throw new BusinessException("使用者不可為空");
        }

        if (!hasText(petName)) {
            throw new BusinessException("寵物名稱不可為空");
        }

        boolean alreadyHasDisplayedPet = petRepository
                .findFirstByUser_UserIdAndIsDisplayedTrue(user.getUserId())
                .isPresent();

        if (alreadyHasDisplayedPet) {
            return;
        }

        PetModel defaultModel = petModelRepository.findById(1)
                .orElseThrow(() -> new ResourceNotFoundException("找不到預設寵物模型"));

        Pet pet = new Pet();
        pet.setPetId(IdGenerator.generate("PET"));
        pet.setUser(user);
        pet.setModel(defaultModel);
        pet.setPetName(petName.trim());
        pet.setMood(MOOD_DEFAULT);
        pet.setCancan(0);
        pet.setIsDisplayed(true);
        pet.setLastUpdateAt(LocalDateTime.now());
        pet.setCreatedAt(LocalDateTime.now());

        petRepository.save(pet);
    }

    /*
     * 套用 mood / cancan 變動量並限制合法區間。
     */
    private void applyDeltas(
            Pet pet,
            int moodDelta,
            int cancanDelta
    ) {
        if (pet == null) {
            throw new BusinessException("寵物資料不可為空");
        }

        int currentMood = safeInt(pet.getMood());
        int currentCancan = safeInt(pet.getCancan());

        pet.setMood(clampMood(currentMood + moodDelta));
        pet.setCancan(clampCancan(currentCancan + cancanDelta));
        pet.setLastUpdateAt(LocalDateTime.now());
    }

    private void validateCurrentUserId(String currentUserId) {
        if (!hasText(currentUserId)) {
            throw new BusinessException("使用者不可為空");
        }
    }

    private int clampMood(int value) {
        return Math.max(MOOD_MIN, Math.min(MOOD_MAX, value));
    }

    private int clampCancan(int value) {
        return Math.max(0, value);
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}