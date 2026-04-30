package com.walletpet.dto.transaction;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class TransactionSummaryResponse {

    private BigDecimal totalIncome;

    private BigDecimal totalExpense;

    private BigDecimal balance;

    private Integer transactionCount;
}