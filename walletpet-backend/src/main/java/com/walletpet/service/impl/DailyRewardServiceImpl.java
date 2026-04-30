package com.walletpet.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.walletpet.dto.dailyreward.DailyRewardResponse;
import com.walletpet.entity.DailyRecordReward;
import com.walletpet.entity.Pet;
import com.walletpet.entity.User;
import com.walletpet.enums.TransactionType;
import com.walletpet.exception.BusinessException;
import com.walletpet.exception.ResourceNotFoundException;
import com.walletpet.repository.DailyRecordRewardRepository;
import com.walletpet.repository.PetRepository;
import com.walletpet.repository.TransactionRepository;
import com.walletpet.repository.UserRepository;
import com.walletpet.service.DailyRewardService;
import com.walletpet.service.PetEventService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class DailyRewardServiceImpl implements DailyRewardService {

    private static final int DAILY_CANCAN_LIMIT = 5;

    private static final String REWARD_TYPE_CAT_FOOD = "CAT_FOOD";

    private static final String EVENT_TYPE_DAILY_BOOKKEEPING_REWARD = "DAILY_BOOKKEEPING_REWARD";

    private final DailyRecordRewardRepository dailyRecordRewardRepository;

    private final UserRepository userRepository;

    private final PetRepository petRepository;

    /*
     * 注意：
     * 這裡使用 TransactionRepository，不使用 TransactionService。
     * 否則會形成：
     * TransactionServiceImpl -> DailyRewardServiceImpl -> TransactionServiceImpl
     * 的循環依賴。
     */
    private final TransactionRepository transactionRepository;

    private final PetEventService petEventService;

    /*
     * TransactionService 新增 / 修改 / 刪除交易後呼叫。
     *
     * 規則：
     * 1. 當日每 1 筆收入 / 支出交易，可得到 cancan +1。
     * 2. 每日最多 cancan +5。
     * 3. 已經發過的 cancan 不重複發。
     * 4. 若交易刪除造成 transactionCount 下降，專題階段先不回收已發 cancan。
     * 5. 每次實際發放 cancan 時，寫入 pet_events。
     */
    @Override
    public DailyRewardResponse handleDailyReward(
            String currentUserId,
            LocalDate rewardDate
    ) {
        validateCurrentUserId(currentUserId);

        LocalDate targetDate = resolveRewardDate(rewardDate);

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到該使用者"));

        Pet pet = petRepository.findFirstByUser_UserIdAndIsDisplayedTrue(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到目前顯示的寵物"));

        int transactionCount = transactionRepository.countByUser_UserIdAndTransactionDateAndTransactionTypeIn(
                currentUserId,
                targetDate,
                Arrays.asList(TransactionType.INCOME, TransactionType.EXPENSE)
        );

        DailyRecordReward reward = dailyRecordRewardRepository
                .findByUser_UserIdAndRewardDate(currentUserId, targetDate)
                .orElseGet(() -> createEmptyReward(user, targetDate));

        int oldCancanDelta = safeInt(reward.getCancanDelta());

        boolean qualified = transactionCount > 0;

        int shouldCancanDelta = qualified
                ? Math.min(transactionCount, DAILY_CANCAN_LIMIT)
                : 0;

        int additionalCancanDelta = shouldCancanDelta - oldCancanDelta;

        reward.setQualified(qualified);
        reward.setTransactionCount(transactionCount);
        reward.setStreakDays(qualified ? 1 : 0);
        reward.setRewardType(REWARD_TYPE_CAT_FOOD);
        reward.setRewardValue(shouldCancanDelta);
        reward.setMoodDelta(0);

        /*
         * 如果刪除交易導致 shouldCancanDelta 小於 oldCancanDelta：
         * 專題階段先不回收已發 cancan。
         *
         * 例如：
         * 原本 5 筆交易，已發 cancanDelta = 5。
         * 刪掉 1 筆後剩 4 筆，shouldCancanDelta = 4。
         * 此時 cancanDelta 仍保留 5，不倒扣寵物 cancan。
         */
        reward.setCancanDelta(Math.max(oldCancanDelta, shouldCancanDelta));
        reward.setUpdatedAt(LocalDateTime.now());

        /*
         * 如果今日交易筆數增加，且尚未達每日 cancan +5 上限，就補發差額。
         *
         * 第 1 筆交易：old = 0, should = 1, additional = 1
         * 第 2 筆交易：old = 1, should = 2, additional = 1
         * 第 6 筆交易：old = 5, should = 5, additional = 0
         */
        if (additionalCancanDelta > 0) {
            pet.setCancan(safeInt(pet.getCancan()) + additionalCancanDelta);
            pet.setLastUpdateAt(LocalDateTime.now());

            reward.setClaimedAt(LocalDateTime.now());

            petEventService.createEvent(
                    user,
                    pet,
                    EVENT_TYPE_DAILY_BOOKKEEPING_REWARD,
                    0,
                    additionalCancanDelta,
                    "每日記帳獎勵：食物量 cancan +" + additionalCancanDelta
            );

            petRepository.save(pet);
        }

        DailyRecordReward savedReward = dailyRecordRewardRepository.save(reward);

        return toResponse(savedReward);
    }

    /*
     * 手動重新計算指定日期。
     *
     * 目前和 handleDailyReward 採同一套規則。
     * Controller 的 /api/rewards/daily/calculate 可呼叫這個方法。
     */
    @Override
    public DailyRewardResponse calculateDailyReward(
            String currentUserId,
            LocalDate rewardDate
    ) {
        return handleDailyReward(currentUserId, rewardDate);
    }

    /*
     * 查詢今日或指定日期每日記帳獎勵狀態。
     *
     * 若尚未有紀錄，不主動發放 cancan，只回傳空狀態。
     */
    @Override
    @Transactional(readOnly = true)
    public DailyRewardResponse getTodayReward(
            String currentUserId,
            LocalDate rewardDate
    ) {
        validateCurrentUserId(currentUserId);

        LocalDate targetDate = resolveRewardDate(rewardDate);

        return dailyRecordRewardRepository
                .findByUser_UserIdAndRewardDate(currentUserId, targetDate)
                .map(this::toResponse)
                .orElseGet(() -> createEmptyResponse(targetDate));
    }

    /*
     * 查詢目前登入者的每日記帳獎勵歷史。
     */
    @Override
    @Transactional(readOnly = true)
    public List<DailyRewardResponse> getRewardHistory(
            String currentUserId
    ) {
        validateCurrentUserId(currentUserId);

        return dailyRecordRewardRepository
                .findByUser_UserIdOrderByRewardDateDesc(currentUserId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private DailyRecordReward createEmptyReward(
            User user,
            LocalDate rewardDate
    ) {
        DailyRecordReward reward = new DailyRecordReward();

        reward.setUser(user);
        reward.setRewardDate(rewardDate);
        reward.setQualified(false);
        reward.setTransactionCount(0);
        reward.setStreakDays(0);
        reward.setRewardType(REWARD_TYPE_CAT_FOOD);
        reward.setRewardValue(0);
        reward.setMoodDelta(0);
        reward.setCancanDelta(0);
        reward.setCreatedAt(LocalDateTime.now());
        reward.setUpdatedAt(LocalDateTime.now());

        return reward;
    }

    private DailyRewardResponse createEmptyResponse(LocalDate rewardDate) {
        DailyRewardResponse response = new DailyRewardResponse();

        response.setRewardDate(rewardDate);
        response.setQualified(false);
        response.setTransactionCount(0);
        response.setStreakDays(0);
        response.setRewardType(REWARD_TYPE_CAT_FOOD);
        response.setRewardValue(0);
        response.setMoodDelta(0);
        response.setCancanDelta(0);

        return response;
    }

    private DailyRewardResponse toResponse(DailyRecordReward reward) {
        DailyRewardResponse response = new DailyRewardResponse();

        response.setDailyRewardId(reward.getDailyRewardId());
        response.setRewardDate(reward.getRewardDate());
        response.setQualified(reward.getQualified());
        response.setTransactionCount(reward.getTransactionCount());
        response.setStreakDays(reward.getStreakDays());
        response.setRewardType(reward.getRewardType());
        response.setRewardValue(reward.getRewardValue());
        response.setMoodDelta(reward.getMoodDelta());
        response.setCancanDelta(reward.getCancanDelta());
        response.setClaimedAt(reward.getClaimedAt());
        response.setCreatedAt(reward.getCreatedAt());
        response.setUpdatedAt(reward.getUpdatedAt());

        return response;
    }

    private LocalDate resolveRewardDate(LocalDate rewardDate) {
        if (rewardDate == null) {
            return LocalDate.now();
        }

        return rewardDate;
    }

    private void validateCurrentUserId(String currentUserId) {
        if (!hasText(currentUserId)) {
            throw new BusinessException("使用者不可為空");
        }
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}