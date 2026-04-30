package com.walletpet.dto.account;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 帳戶總覽頁的摘要 DTO。
 *
 * DTO 的用途是「前後端傳資料用」，不要直接把 Entity 回傳給前端。
 * 這個物件專門給帳戶總覽頁顯示：
 * 1. 總資產 totalAssets：一般資產帳戶的餘額加總。
 * 2. 總負債 totalLiabilities：負債帳戶的餘額加總，前端通常用正數顯示欠款。
 * 3. 淨資產 netWorth：總資產 - 總負債。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountSummaryResponse {

    /** 一般資產帳戶餘額加總，例如現金、銀行、存款帳戶。 */
    private BigDecimal totalAssets;

    /** 負債帳戶餘額加總，例如信用卡、貸款。 */
    private BigDecimal totalLiabilities;

    /** 淨資產 = 總資產 - 總負債。 */
    private BigDecimal netWorth;
}