package com.walletpet.dto.transaction;

import java.util.List;

import lombok.Data;

@Data
public class TransactionListResponse {

    private TransactionSummaryResponse summary;

    private List<TransactionResponse> items;

    private int page;

    private int size;

    private long totalElements;

    private int totalPages;

    private boolean first;

    private boolean last;
}