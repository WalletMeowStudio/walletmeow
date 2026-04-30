package com.walletpet.service;

import java.math.BigDecimal;
import java.util.List;

import com.walletpet.dto.budget.BudgetResult;
import com.walletpet.entity.Budget;

public interface BudgetService {
	public List<BudgetResult> getAllBudgetProgress(String userId);
	
	public Budget updateBudgetAmount(String budgetId, BigDecimal newAmount);
	 public void deleteBudget(String budgetId);
	 public Budget getBudgetById(String budgetId);
	 public Budget createBudget(String userId, Budget budget);
}
