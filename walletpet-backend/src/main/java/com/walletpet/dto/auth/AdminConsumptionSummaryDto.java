package com.walletpet.dto.auth;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class AdminConsumptionSummaryDto {
	private String userId;
    private String userName;
    private BigDecimal totalExpense;
    private BigDecimal totalIncome;
    private BigDecimal avgDailyExpense;
    private String topCategoryName;
    private Integer transactionCount;

}
