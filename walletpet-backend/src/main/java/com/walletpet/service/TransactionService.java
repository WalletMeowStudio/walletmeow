package com.walletpet.service;

import java.time.LocalDate;

import com.walletpet.dto.transaction.TransactionCreateRequest;
import com.walletpet.dto.transaction.TransactionFormMetaResponse;
import com.walletpet.dto.transaction.TransactionListResponse;
import com.walletpet.dto.transaction.TransactionResponse;
import com.walletpet.dto.transaction.TransactionSummaryResponse;
import com.walletpet.dto.transaction.TransactionUpdateRequest;
import com.walletpet.enums.TransactionType;

public interface TransactionService {

    TransactionFormMetaResponse getFormMeta(String currentUserId,TransactionType transactionType);

    TransactionResponse createTransaction(String currentUserId,TransactionCreateRequest request);

    TransactionListResponse searchTransactions(
            String currentUserId,LocalDate startDate,LocalDate endDate,Integer accountId,
            String categoryId,TransactionType type,int page,int size);

    TransactionResponse findById(String currentUserId,String transactionId);

    TransactionResponse updateTransaction(String currentUserId,String transactionId,
    		TransactionUpdateRequest request);

    TransactionResponse deleteTransaction(String currentUserId,String transactionId);

    TransactionSummaryResponse getSummary(String currentUserId,LocalDate startDate,
            LocalDate endDate,Integer accountId,String categoryId);
    
    //每日任務用
    int countDailyBookkeepingTransactions(String currentUserId,LocalDate transactionDate);
}