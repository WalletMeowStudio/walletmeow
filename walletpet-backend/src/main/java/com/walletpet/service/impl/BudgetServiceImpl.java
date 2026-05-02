package com.walletpet.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.walletpet.dto.budget.BudgetResult;
import com.walletpet.entity.Budget;
import com.walletpet.entity.Category;
import com.walletpet.entity.User;
import com.walletpet.repository.BudgetRepository;
import com.walletpet.repository.CategoryRepository;
import com.walletpet.service.BudgetService;
import com.walletpet.service.TransactionService;
import com.walletpet.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class BudgetServiceImpl implements BudgetService {
	
	private final UserService userService;
	
    private final BudgetRepository budgetRepository;
   
    private final TransactionService transactionService; 
    private final CategoryRepository categoryRepository;
   
    //private final TransactionRepository transactionRepository;
    
    @Override
    public Budget getBudgetById(String budgetId) {
        return budgetRepository.findById(budgetId)
                .orElseThrow(() -> new RuntimeException("找不到該預算 ID: " + budgetId));
    }

   

    @Override
    public List<BudgetResult> getAllBudgetProgress(String userId) {
        // 1. 先抓出該用戶所有的預算設定
        User user = new User();
        user.setUserId(userId);
        List<Budget> budgets = budgetRepository.findByUser(user);

        return budgets.stream().map(budget -> {
            // 2. 獲取該預算的支出總額
            // 注意：如果是整體預算，categoryId 可能為 null
            String categoryId = (budget.getCategory() != null) ? budget.getCategory().getCategoryId() : null;

            Map<String, Object> summary = transactionService.getSummary(
                    userId,
                    budget.getStartDate(),
                    budget.getEndDate(),
                    null,
                    categoryId
            );

            // 3. 從 Summary 直接拿到算好的總支出
            BigDecimal totalSpent = getBigDecimal(summary, "totalExpense");

            BudgetResult result = new BudgetResult();
            result.setBudget(budget);
            result.setCurrentSpent(totalSpent);
            
            // --- 【新增：搬運分類名字】 ---
            // 既然你不敢亂動別人的東西，我們就只讀取名字字串
            if (budget.getCategory() != null) {
                result.setCategoryName(budget.getCategory().getCategoryName());
            } else if ("TOTAL".equals(budget.getTargetType())) {
                result.setCategoryName("整體支出");
            }
            // ----------------------------

            // 4. 計算百分比
            if (budget.getBudgetAmount() != null && budget.getBudgetAmount().compareTo(BigDecimal.ZERO) > 0) {
                result.setProgress(totalSpent.divide(budget.getBudgetAmount(), 4, RoundingMode.HALF_UP).doubleValue());
            } else {
                result.setProgress(0.0);
            }
            return result;
        }).collect(Collectors.toList());
    }

    public Budget createBudget(String userId, Budget budget) {
        // 1. 設置使用者關聯
        User user = userService.getUserEntityById(userId); 
        budget.setUser(user);

        // 2. 修復主鍵 UUID
        if (budget.getBudgetId() == null || budget.getBudgetId().trim().isEmpty()) {
            budget.setBudgetId(java.util.UUID.randomUUID().toString());
        }

        // --- 【關鍵修正：強制對接 Category ID】 ---
        // 確保即使 Jackson 解析層級有誤，我們也在存檔前手動把關聯建立起來
        if (budget.getCategory() != null && budget.getCategory().getCategoryId() != null) {
            // 這裡會印在後台 Console，你可以確認 ES002 有沒有出現
            System.out.println("偵測到分類 ID: " + budget.getCategory().getCategoryId());
        } else {
            System.out.println("警告：收到的 Budget 物件中沒有分類資訊！");
        }
        // ---------------------------------------

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

    /*
     * TransactionService.getSummary() 現在回傳 Map<String, Object>。
     * 這個方法負責把 summary 裡面的 totalIncome / totalExpense / balance 安全轉成 BigDecimal。
     */
    private BigDecimal getBigDecimal(Map<String, Object> map, String key) {
        Object value = map.get(key);

        if (value == null) {
            return BigDecimal.ZERO;
        }

        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }

        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }

        return new BigDecimal(value.toString());
    }
    public Budget createBudgetWithId(String userId, Budget budget, String categoryId) {
        // 1. 設置用戶
        User user = userService.getUserEntityById(userId); 
        budget.setUser(user);

        // 2. 設置 UUID
        if (budget.getBudgetId() == null) {
            budget.setBudgetId(java.util.UUID.randomUUID().toString());
        }

        // 3. 關鍵對接：如果前端有傳 ID 過來
        if (categoryId != null && !categoryId.isEmpty()) {
            Category cat = new Category();
            cat.setCategoryId(categoryId); // 建立一個只有 ID 的殼子
            budget.setCategory(cat);       // 強制塞進 Budget 物件
        }

        return budgetRepository.save(budget);
    }
}