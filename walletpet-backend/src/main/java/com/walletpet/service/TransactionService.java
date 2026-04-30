package com.walletpet.service;

import java.time.LocalDate;
import java.util.Map;

import com.walletpet.dto.transaction.TransactionCreateRequest;
import com.walletpet.dto.transaction.TransactionResponse;
import com.walletpet.dto.transaction.TransactionUpdateRequest;
import com.walletpet.enums.TransactionType;

public interface TransactionService {

    Map<String, Object> getFormMeta(
            String currentUserId,
            TransactionType transactionType
    );

    TransactionResponse createTransaction(
            String currentUserId,
            TransactionCreateRequest request
    );

    Map<String, Object> searchTransactions(
            String currentUserId,
            LocalDate startDate,
            LocalDate endDate,
            Integer accountId,
            String categoryId,
            TransactionType type,
            int page,
            int size
    );

    TransactionResponse findById(
            String currentUserId,
            String transactionId
    );

    TransactionResponse updateTransaction(
            String currentUserId,
            String transactionId,
            TransactionUpdateRequest request
    );

    TransactionResponse deleteTransaction(
            String currentUserId,
            String transactionId
    );

    Map<String, Object> getSummary(
            String currentUserId,
            LocalDate startDate,
            LocalDate endDate,
            Integer accountId,
            String categoryId
    );

    /*
     * 每日任務用：
     * 計算某使用者某一天的收入 / 支出交易筆數。
     */
    int countDailyBookkeepingTransactions(
            String currentUserId,
            LocalDate transactionDate
    );
}