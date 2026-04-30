package com.walletpet.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.walletpet.dto.budget.BudgetResult;
import com.walletpet.dto.common.ApiResponse;
import com.walletpet.entity.Budget;
import com.walletpet.security.CurrentUserUtil;
import com.walletpet.service.BudgetService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor // 參考同學用的 Lombok 注入方式，簡潔很多
public class BudgetController {

    private final BudgetService budgetService;
    
    // 注入同學寫好的 CurrentUserUtil 工具類
    private final CurrentUserUtil currentUserUtil;

    /**
     * 獲取該用戶所有預算及其即時進度
     * 現在不需要在 RequestParam 傳 userId 了，直接從 Token 抓
     */
    @GetMapping
    public ApiResponse<List<BudgetResult>> getAllBudgets() {
        // 使用同學的工具類獲取當前登入者的 ID
        String currentUserId = currentUserUtil.getCurrentUserId();
        
        List<BudgetResult> data = budgetService.getAllBudgetProgress(currentUserId);
        
        return ApiResponse.success("查詢預算進度成功", data);
    }

    /**
     * 建立預算
     */
    @PostMapping
    public ApiResponse<Budget> createBudget(@RequestBody Budget budget) {
        // 1. 科學的第一步：從 Token 抓出「到底是誰」在發請求
        String currentUserId = currentUserUtil.getCurrentUserId();
        
        // 2. 科學的第二步：把預算物件「連同用戶 ID」一起交給 Service
        // 不要讓前端傳 User 物件，由我們後端來綁定
        Budget savedBudget = budgetService.createBudget(currentUserId, budget);
        
        return ApiResponse.success("建立預算成功", savedBudget);
    }

    /**
     * 查看單一預算詳細
     */
    @GetMapping("/{id}")
    public ApiResponse<Budget> getBudgetById(@PathVariable String id) {
        Budget budget = budgetService.getBudgetById(id);
        return ApiResponse.success("查詢成功", budget);
    }

    /**
     * 修改預算金額
     */
    @PutMapping("/{id}")
    public ApiResponse<Budget> updateBudget(@PathVariable String id, @RequestBody BigDecimal newAmount) {
        Budget updatedBudget = budgetService.updateBudgetAmount(id, newAmount);
        return ApiResponse.success("預算修改成功", updatedBudget);
    }

    /**
     * 刪除預算
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteBudget(@PathVariable String id) {
        budgetService.deleteBudget(id);
        return ApiResponse.success("預算刪除成功", null);
    }
}

