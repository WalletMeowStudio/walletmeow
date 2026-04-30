package com.walletpet.dto.account;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * 帳戶 API 回傳給前端的資料格式。
 *
 * Entity 會有完整關聯，例如 user、incomingTransfers、outgoingTransfers，
 * 但前端帳戶列表不需要全部資料，所以用 DTO 控制回傳欄位。
 */
@Data
public class AccountResponse {

    private Integer accountId;

    private String userId;

    private String accountName;

    private BigDecimal balance;

    private Boolean isLiability;

    private Boolean isSavingAccount;

    private Boolean isDeleted;

    private LocalDateTime createdAt;
}
