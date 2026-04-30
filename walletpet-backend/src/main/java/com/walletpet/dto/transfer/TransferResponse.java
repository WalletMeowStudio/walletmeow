package com.walletpet.dto.transfer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * 轉帳 API 回傳給前端的資料格式。
 *
 * 除了轉帳本身的資料，也一併回傳轉出／轉入帳戶名稱與最新餘額，
 * 讓前端轉帳完成後可以直接更新畫面。
 */
@Data
public class TransferResponse {

    private Integer accountTransId;

    private Integer fromAccountId;

    private String fromAccountName;

    private BigDecimal fromAccountBalance;

    private Integer toAccountId;

    private String toAccountName;

    private BigDecimal toAccountBalance;

    private BigDecimal transactionAmount;

    private LocalDate transactionDate;

    private String note;

    private LocalDateTime createdAt;
}
