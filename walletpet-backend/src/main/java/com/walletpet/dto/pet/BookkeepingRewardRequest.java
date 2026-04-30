package com.walletpet.dto.pet;

import lombok.Data;

/**
 * 記帳獎勵請求：使用者每完成一次記帳就呼叫一次，後端會：
 *   - 寵物 cancan +1 (但同一天最多 +5)
 *   - 寫一筆 pet_event (event_type = "BOOKKEEPING")
 * transactionId 可選，留作未來追蹤該獎勵來源於哪一筆交易。
 */
@Data
public class BookkeepingRewardRequest {
    private String userId;
    private String petId;
    private String transactionId;
}
