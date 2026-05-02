package com.walletpet.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.walletpet.dto.pet.LoginTickResponse;
import com.walletpet.dto.pet.PetResponse;
import com.walletpet.entity.Pet;
import com.walletpet.entity.User;
import com.walletpet.entity.UserLoginLog;
import com.walletpet.exception.BusinessException;
import com.walletpet.exception.ResourceNotFoundException;
import com.walletpet.mapper.PetMapper;
import com.walletpet.repository.UserLoginLogRepository;
import com.walletpet.repository.UserRepository;
import com.walletpet.service.LoginStreakService;
import com.walletpet.service.PetEventService;
import com.walletpet.service.PetService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class LoginStreakServiceImpl implements LoginStreakService {

    private static final int MOOD_DEFAULT = 60;

    private static final int STREAK_3_BONUS = 5;

    private static final int STREAK_7_BONUS = 10;

    private static final int MISS_3_PENALTY = -10;

    private static final int MISS_7_PENALTY = -20;

    private static final String EVENT_TYPE_LOGIN_STREAK_3 = "LOGIN_STREAK_3";

    private static final String EVENT_TYPE_LOGIN_STREAK_7 = "LOGIN_STREAK_7";

    private static final String EVENT_TYPE_LOGIN_ABSENCE_3 = "LOGIN_ABSENCE_3";

    private static final String EVENT_TYPE_LOGIN_ABSENCE_7 = "LOGIN_ABSENCE_7";

    private static final String EVENT_TYPE_MOOD_RECOVERY_60 = "MOOD_RECOVERY_60";

    private final UserRepository userRepository;

    private final UserLoginLogRepository userLoginLogRepository;

    private final PetService petService;

    private final PetEventService petEventService;

    /*
     * 登入 tick。
     *
     * 正式環境 loginDate 可傳 null，後端自動使用今天。
     * 測試 streak 時，可以暫時從 Controller 開放 date query param。
     */
    @Override
    public LoginTickResponse loginTick(
            String currentUserId,
            LocalDate loginDate
    ) {
        validateCurrentUserId(currentUserId);

        LocalDate targetDate = loginDate == null ? LocalDate.now() : loginDate;

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到該使用者"));

        Pet pet = petService.getDisplayedPetEntity(currentUserId);

        /*
         * 同一天重複呼叫：
         * 不新增 user_login_logs
         * 不重複加減 mood
         * 不寫 pet_events
         */
        if (userLoginLogRepository.existsByUser_UserIdAndLoginDate(currentUserId, targetDate)) {
            int streakDays = calculateStreakDays(currentUserId, targetDate);

            return buildResponse(
                    targetDate,
                    true,
                    streakDays,
                    0,
                    0,
                    false,
                    null,
                    PetMapper.toResponse(pet)
            );
        }

        Integer missedDays = calculateMissedDays(currentUserId, targetDate);

        UserLoginLog loginLog = new UserLoginLog();
        loginLog.setUser(user);
        loginLog.setLoginDate(targetDate);
        loginLog.setCreatedAt(LocalDateTime.now());
        userLoginLogRepository.save(loginLog);

        int streakDays = calculateStreakDays(currentUserId, targetDate);

        LoginMoodRuleResult moodRule = resolveMoodRule(
                safeInt(pet.getMood()),
                streakDays,
                missedDays
        );

        if (moodRule.getMoodDelta() != 0) {
            int newMood = clampMood(safeInt(pet.getMood()) + moodRule.getMoodDelta());
            pet.setMood(newMood);
            pet.setLastUpdateAt(LocalDateTime.now());

            petEventService.createEvent(
                    user,
                    pet,
                    moodRule.getEventType(),
                    moodRule.getMoodDelta(),
                    0,
                    moodRule.getRewardMessage()
            );
        }

        PetResponse petResponse = PetMapper.toResponse(pet);

        return buildResponse(
                targetDate,
                false,
                streakDays,
                missedDays,
                moodRule.getMoodDelta(),
                moodRule.isMoodRecoveredTo60(),
                moodRule.getEventType(),
                petResponse
        );
    }

    /*
     * 缺席天數：
     * 上次登入日為 D，本次為 T。
     * missedDays = T - D - 1。
     *
     * 例：
     * 上次 4/24，本次 4/27：
     * 4/25、4/26 沒登入，所以 missedDays = 2。
     */
    private Integer calculateMissedDays(
            String currentUserId,
            LocalDate loginDate
    ) {
        return userLoginLogRepository
                .findTop1ByUser_UserIdAndLoginDateBeforeOrderByLoginDateDesc(
                        currentUserId,
                        loginDate
                )
                .map(lastLog -> {
                    long diffDays = ChronoUnit.DAYS.between(
                            lastLog.getLoginDate(),
                            loginDate
                    );

                    if (diffDays <= 1) {
                        return 0;
                    }

                    return (int) diffDays - 1;
                })
                .orElse(null);
    }

    /*
     * 計算包含 loginDate 當天在內的連續登入天數。
     */
    private int calculateStreakDays(
            String currentUserId,
            LocalDate loginDate
    ) {
        LocalDate startDate = loginDate.minusDays(30);

        List<UserLoginLog> logs = userLoginLogRepository
                .findByUser_UserIdAndLoginDateBetweenOrderByLoginDateDesc(
                        currentUserId,
                        startDate,
                        loginDate
                );

        Set<LocalDate> loginDates = new HashSet<>();

        for (UserLoginLog log : logs) {
            loginDates.add(log.getLoginDate());
        }

        int streakDays = 0;
        LocalDate cursor = loginDate;

        while (loginDates.contains(cursor)) {
            streakDays++;
            cursor = cursor.minusDays(1);
        }

        return streakDays;
    }

    /*
     * 規則優先序：
     * 1. 缺席 7 天以上：-20
     * 2. 缺席 3 天以上：-10
     * 3. 連續登入 7 天：
     *    - 若 mood < 60，補到 60
     *    - 否則 +10
     * 4. 連續登入 3 天：+5
     */
    private LoginMoodRuleResult resolveMoodRule(
            int currentMood,
            int streakDays,
            Integer missedDays
    ) {
        if (missedDays != null && missedDays >= 7) {
            return new LoginMoodRuleResult(
                    MISS_7_PENALTY,
                    false,
                    EVENT_TYPE_LOGIN_ABSENCE_7,
                    "缺席 7 天以上：心情值 mood -20"
            );
        }

        if (missedDays != null && missedDays >= 3) {
            return new LoginMoodRuleResult(
                    MISS_3_PENALTY,
                    false,
                    EVENT_TYPE_LOGIN_ABSENCE_3,
                    "缺席 3 天以上：心情值 mood -10"
            );
        }

        if (streakDays == 7) {
            if (currentMood < MOOD_DEFAULT) {
                int recoverDelta = MOOD_DEFAULT - currentMood;

                return new LoginMoodRuleResult(
                        recoverDelta,
                        true,
                        EVENT_TYPE_MOOD_RECOVERY_60,
                        "連續登入 7 天且 mood 低於 60：心情值回復到 60"
                );
            }

            return new LoginMoodRuleResult(
                    STREAK_7_BONUS,
                    false,
                    EVENT_TYPE_LOGIN_STREAK_7,
                    "連續登入 7 天：心情值 mood +10"
            );
        }

        if (streakDays == 3) {
            return new LoginMoodRuleResult(
                    STREAK_3_BONUS,
                    false,
                    EVENT_TYPE_LOGIN_STREAK_3,
                    "連續登入 3 天：心情值 mood +5"
            );
        }

        return new LoginMoodRuleResult(
                0,
                false,
                null,
                null
        );
    }

    private LoginTickResponse buildResponse(
            LocalDate loginDate,
            Boolean alreadyLoggedToday,
            Integer loginStreakDays,
            Integer missedDays,
            Integer moodDelta,
            Boolean moodRecoveredTo60,
            String eventType,
            PetResponse pet
    ) {
        LoginTickResponse response = new LoginTickResponse();

        response.setLoginDate(loginDate);
        response.setAlreadyLoggedToday(alreadyLoggedToday);
        response.setLoginStreakDays(loginStreakDays);
        response.setMissedDays(missedDays);
        response.setMoodDelta(moodDelta);
        response.setMoodRecoveredTo60(moodRecoveredTo60);
        response.setEventType(eventType);
        response.setPet(pet);
        response.setCreatedAt(LocalDateTime.now());

        return response;
    }

    private void validateCurrentUserId(String currentUserId) {
        if (!hasText(currentUserId)) {
            throw new BusinessException("使用者不可為空");
        }
    }

    private int clampMood(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static class LoginMoodRuleResult {

        private final int moodDelta;

        private final boolean moodRecoveredTo60;

        private final String eventType;

        private final String rewardMessage;

        LoginMoodRuleResult(
                int moodDelta,
                boolean moodRecoveredTo60,
                String eventType,
                String rewardMessage
        ) {
            this.moodDelta = moodDelta;
            this.moodRecoveredTo60 = moodRecoveredTo60;
            this.eventType = eventType;
            this.rewardMessage = rewardMessage;
        }

        int getMoodDelta() {
            return moodDelta;
        }

        boolean isMoodRecoveredTo60() {
            return moodRecoveredTo60;
        }

        String getEventType() {
            return eventType;
        }

        String getRewardMessage() {
            return rewardMessage;
        }
    }
}