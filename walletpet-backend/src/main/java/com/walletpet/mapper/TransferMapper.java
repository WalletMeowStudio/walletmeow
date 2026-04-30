package com.walletpet.mapper;

import com.walletpet.dto.transfer.TransferResponse;
import com.walletpet.entity.AccountTransaction;

/**
 * TransferMapper 負責把 AccountTransaction Entity 轉成 TransferResponse DTO。
 *
 * 因為 AccountTransaction 裡面連到 fromAccount、toAccount，
 * 但前端只需要帳戶 id、名稱、最新餘額等資訊，
 * 所以在這裡整理成乾淨的回傳格式。
 */
public class TransferMapper {

    private TransferMapper() {
    }

    public static TransferResponse toResponse(AccountTransaction transfer) {
        if (transfer == null) {
            return null;
        }

        TransferResponse response = new TransferResponse();
        response.setAccountTransId(transfer.getAccountTransId());
        response.setTransactionAmount(transfer.getTransactionAmount());
        response.setTransactionDate(transfer.getTransactionDate());
        response.setNote(transfer.getNote());
        response.setCreatedAt(transfer.getCreatedAt());

        // 轉出帳戶資料，對應 account_transactions.from_account_id。
        if (transfer.getFromAccount() != null) {
            response.setFromAccountId(transfer.getFromAccount().getAccountId());
            response.setFromAccountName(transfer.getFromAccount().getAccountName());
            response.setFromAccountBalance(transfer.getFromAccount().getBalance());
        }

        // 轉入帳戶資料，對應 account_transactions.to_account_id。
        if (transfer.getToAccount() != null) {
            response.setToAccountId(transfer.getToAccount().getAccountId());
            response.setToAccountName(transfer.getToAccount().getAccountName());
            response.setToAccountBalance(transfer.getToAccount().getBalance());
        }

        return response;
    }
}
