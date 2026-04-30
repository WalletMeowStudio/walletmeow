package com.walletpet.dto.pet;

import lombok.Data;

/**
 * 寵物餵食請求。
 *
 * feedType 列舉值：
 *   CAN   - 餵罐罐  (-1 cancan, +1 mood, 受每日 +3 mood 上限)
 *   FISH  - 餵小魚乾 (-1 cancan, +1 mood, 受每日 +3 mood 上限)
 *   SNACK - 餵零食   (-1 cancan, +1 mood, 受每日 +3 mood 上限)
 *   FEAST - 大餐     (-10 cancan, +15 mood, 不受每日 mood 上限)
 */
@Data
public class PetFeedRequest {
    private String userId;
    private String petId;
    private String feedType;
}
