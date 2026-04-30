package com.walletpet.service;

import java.math.BigDecimal;
import java.util.List;

import com.walletpet.dto.savinggoal.SavingGoalRequest;
import com.walletpet.entity.SavingGoal;

public interface SavingGoalService {
	public SavingGoal createSavingGoal(String currentUserId, SavingGoalRequest request);

	public void deposit(String currentUserId, String goalId, BigDecimal amount, Integer fromAccountId);

	public SavingGoal completeGoal(String currentUserId, String goalId);

	public SavingGoal updateGoal(String currentUserId, String goalId, SavingGoalRequest request);

	public void deleteGoal(String currentUserId, String goalId, Integer toAccountId);
	
	public List<SavingGoal> findAll(String currentUserId);
}
