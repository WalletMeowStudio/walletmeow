package com.walletpet.dto.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.walletpet.enums.TransactionType;

import lombok.Data;

@Data
public class TransactionResponse {

    private String transactionId;

    private TransactionType transactionType;

    private Integer accountId;

    private String accountName;

    private String categoryId;

    private String categoryName;

    private BigDecimal transactionAmount;

    private LocalDate transactionDate;

    private String note;

    private LocalDateTime createdAt;
}