package com.walletpet.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.walletpet.entity.Budget;
import com.walletpet.entity.Category;
import com.walletpet.entity.User;

public interface BudgetRepository extends JpaRepository<Budget, String>{
	// 1. 最核心：列出該用戶的所有預算計畫
    List<Budget> findByUser(User user);
    
    // 2. (選配) 如果你想檢查該用戶是否在同個分類已經設過預算了
    boolean existsByUserAndCategory(User user, Category category);
}
