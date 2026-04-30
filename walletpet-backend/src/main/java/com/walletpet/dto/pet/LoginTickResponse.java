package com.walletpet.dto.pet;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * 登入 tick 回傳：包含套用 streak 規則後的最新寵物狀態。
 *
 * streak 規則（在 PetServiceImpl#applyLoginTick 實作）：
 *   - 連續登入 3 天   → mood +5
 *   - 連續登入 7 天   → mood +10
 *   - 缺席 3 天再登入 → mood -10
 *   - 缺席 7 天再登入 → mood -20
 *   - 連續 7 天登入且 mood < 60 → 直接拉回 60
 */
@Data
public class LoginTickResponse {
   
	private LocalDate loginDate;
	
	private Boolean alreadyLoggedToday;// 今天是否為第一次登入（避免重複 tick）

    private Integer loginStreakDays;// 連續登入天數（含本次）

    private Integer missedDays;// 距上次登入隔了幾天（首次登入為 null）

    private Integer moodDelta;// 本次登入 tick 套用後的 mood 變動量

    private Boolean moodRecoveredTo60;

    private String eventType;// STREAK_3 / STREAK_7 / MISS_3 / MISS_7 / STREAK_7_RECOVER / NONE

    private PetResponse pet;

    private LocalDateTime createdAt;
}