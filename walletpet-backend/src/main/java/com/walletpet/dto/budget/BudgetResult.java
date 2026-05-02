package com.walletpet.dto.budget;

import java.math.BigDecimal;

import com.walletpet.entity.Budget;

import lombok.Data;

@Data
public class BudgetResult {
	public Budget budget;          // 預算設定
	public BigDecimal currentSpent; // 算好的錢
	public double progress;         // 算好的百分比
	public String categoryName;
}
