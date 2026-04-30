package com.walletpet.dto.savinggoal;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

@Data
public class SavingGoalRequest {
    // 1. 建立目標時需要填的
    private String goalName;      // 目標名稱
    private BigDecimal targetAmount; // 目標金額
    private LocalDate startDate;  // 開始日期
    private LocalDate endDate;    // 結束日期
    private Integer accountId;    // 綁定的帳戶 ID (對應你 Entity 裡的 account_id)

    /* 如果是修改 (Update)，前端一樣傳這個包裹回來，
       但你在 Service 邏輯裡只會拿 goalName 和 targetAmount，
       其他的格子就算前端亂填，你也不會去理它。
    */
}