package com.walletpet.dto.transfer;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 刪除／回沖轉帳後回傳的帳戶餘額資料。
 *
 * 目前用於 DELETE /api/transfers/{id}，讓前端知道帳戶餘額已被還原。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferAccountBalanceResponse {

    private Integer accountId;

    private Boolean isDeleted;

    private BigDecimal balance;
}
