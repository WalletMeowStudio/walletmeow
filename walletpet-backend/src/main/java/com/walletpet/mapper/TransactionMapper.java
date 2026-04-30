package com.walletpet.mapper;

import com.walletpet.dto.transaction.TransactionResponse;
import com.walletpet.entity.Transaction;

public class TransactionMapper {

    private TransactionMapper() {

    }

    public static TransactionResponse toResponse(Transaction transaction) {
        if (transaction == null) {
            return null;
        }

        TransactionResponse response = new TransactionResponse();

        response.setTransactionId(transaction.getTransactionId());
        response.setTransactionType(transaction.getTransactionType());
        response.setTransactionAmount(transaction.getTransactionAmount());
        response.setTransactionDate(transaction.getTransactionDate());
        response.setNote(transaction.getNote());
        response.setCreatedAt(transaction.getCreatedAt());

        if (transaction.getAccount() != null) {
            response.setAccountId(transaction.getAccount().getAccountId());
            response.setAccountName(transaction.getAccount().getAccountName());
        }

        if (transaction.getCategory() != null) {
            response.setCategoryId(transaction.getCategory().getCategoryId());
            response.setCategoryName(transaction.getCategory().getCategoryName());
        }

        return response;
    }
}