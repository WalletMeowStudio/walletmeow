package com.walletpet.dto.transfer;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

/**
 * 新增轉帳 API 的前端傳入資料。
 *
 * POST /api/transfers
 * 代表使用者要把一筆錢從 fromAccountId 轉到 toAccountId。
 */
@Data
public class TransferCreateRequest {

    /** 轉出帳戶 id，對應 account_transactions.from_account_id。 */
    private Integer fromAccountId;

    /** 轉入帳戶 id，對應 account_transactions.to_account_id。 */
    private Integer toAccountId;

    /** 轉帳金額，必須大於 0。 */
    private BigDecimal transactionAmount;

    /** 轉帳日期；如果前端沒傳，Service 會預設為今天。 */
    private LocalDate transactionDate;

    /** 備註。 */
    private String note;
}
