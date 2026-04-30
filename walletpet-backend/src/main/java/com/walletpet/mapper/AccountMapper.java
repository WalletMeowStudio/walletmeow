package com.walletpet.mapper;

import com.walletpet.dto.account.AccountResponse;
import com.walletpet.entity.Account;

/**
 * AccountMapper 負責把 Account Entity 轉成 AccountResponse DTO。
 *
 * 這樣 Controller 不需要知道 Entity 裡面有哪些關聯，
 * 前端也只會收到需要顯示的欄位。
 */
public class AccountMapper {

    private AccountMapper() {

    }

    public static AccountResponse toResponse(Account account) {
        if (account == null) {
            return null;
        }

        AccountResponse response = new AccountResponse();

        response.setAccountId(account.getAccountId());

        if (account.getUser() != null) {
            response.setUserId(account.getUser().getUserId());
        }

        response.setAccountName(account.getAccountName());
        response.setBalance(account.getBalance());
        response.setIsLiability(account.getIsLiability());
        response.setIsSavingAccount(account.getIsSavingAccount());
        response.setIsDeleted(account.getIsDeleted());
        response.setCreatedAt(account.getCreatedAt());

        return response;
    }
}
