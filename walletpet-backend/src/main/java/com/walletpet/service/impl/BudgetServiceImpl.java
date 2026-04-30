package com.walletpet.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.walletpet.dto.budget.BudgetResult;
import com.walletpet.dto.transaction.TransactionSummaryResponse;
import com.walletpet.entity.Budget;
import com.walletpet.entity.Transaction;
import com.walletpet.entity.User;
import com.walletpet.repository.BudgetRepository;
import com.walletpet.repository.TransactionRepository;
import com.walletpet.service.BudgetService;
import com.walletpet.service.TransactionService;
import com.walletpet.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class BudgetServiceImpl implements BudgetService{
	
	private final UserService userService;
	
    private final BudgetRepository budgetRepository;
   
    private final TransactionService transactionService; 
   
    //private final TransactionRepository transactionRepository;
    
    @Override
    public Budget getBudgetById(String budgetId) {
        return budgetRepository.findById(budgetId)
                .orElseThrow(() -> new RuntimeException("找不到該預算 ID: " + budgetId));
    }

   

    @Override
    public List<BudgetResult> getAllBudgetProgress(String userId) {
        // 先抓出該用戶所有的預算設定
        User user = new User();
        user.setUserId(userId);
        List<Budget> budgets = budgetRepository.findByUser(user);

        return budgets.stream().map(budget -> {
            // 2. 直接接他的查總額功能
            // 傳入：用戶ID、預算開始日、結束日、不限帳戶(null)、指定該預算的分類ID
            TransactionSummaryResponse summary = transactionService.getSummary(
                    userId,
                    budget.getStartDate(),
                    budget.getEndDate(),
                    null,
                    budget.getCategory().getCategoryId()
            );

            // 3. 從 Summary 直接拿到算好的總支出
            BigDecimal totalSpent = summary.getTotalExpense();

            BudgetResult result = new BudgetResult();
            result.setBudget(budget);
            result.setCurrentSpent(totalSpent);
            
            // 計算百分比
            if (budget.getBudgetAmount().compareTo(BigDecimal.ZERO) > 0) {
                result.setProgress(totalSpent.divide(budget.getBudgetAmount(), 4, RoundingMode.HALF_UP).doubleValue());
            } else {
                result.setProgress(0.0);
            }
            return result;
        }).collect(Collectors.toList());
    }

    // 2. 建立預算
    public Budget createBudget(String userId, Budget budget) {
        // 1. 根據 ID 去資料庫把真正的 User 撈出來
    	

    	User user = userService.getUserEntityById(userId); 

        budget.setUser(user);
        return budgetRepository.save(budget);
    }

    // 3. 修改預算金額 (截圖需求：只修改上限金額)
    @Override
    public Budget updateBudgetAmount(String budgetId, BigDecimal newAmount) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new RuntimeException("找不到該預算"));
        
        // A1：輸入無效（例如負數）則不允許儲存
        if (newAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("預算金額不可為負數");
        }
        
        budget.setBudgetAmount(newAmount);
        return budgetRepository.save(budget);
    }

    // 4. 刪除預算 (截圖需求：移除預算資料，但支出資料保留)
    @Override
    public void deleteBudget(String budgetId) {
        // 直接刪除預算表內的資料即可，因為沒有強關聯，所以不會動到 Transaction 表
        budgetRepository.deleteById(budgetId);
    }
}