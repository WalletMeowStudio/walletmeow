package com.walletpet.dto.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.walletpet.enums.TransactionType;

import lombok.Data;

@Data
public class TransactionCreateRequest {

    private TransactionType transactionType;

    private Integer accountId;

    private String categoryId;

    private BigDecimal transactionAmount;

    private LocalDate transactionDate;

    private String note;
}