package com.walletpet.dto.account;

import lombok.Data;

/**
 * 修改帳戶 API 的前端傳入資料。
 *
 * PUT /api/accounts/{id}
 * 這裡不放 balance，因為餘額應由收入、支出、轉帳去更新，
 * 避免使用者直接改餘額造成帳務不一致。
 */
@Data
public class AccountUpdateRequest {

    private String accountName;

    private Boolean isLiability;

    private Boolean isSavingAccount;
}
