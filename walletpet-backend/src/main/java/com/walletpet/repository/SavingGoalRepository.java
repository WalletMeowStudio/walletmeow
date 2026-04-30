package com.walletpet.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.walletpet.entity.Account;
import com.walletpet.entity.SavingGoal;
import com.walletpet.entity.User;

@Repository
public interface SavingGoalRepository extends JpaRepository<SavingGoal, String> {

	/**
	 * 1. 列出該用戶的所有存款目標 用於前台「我的存款」頁面顯示列表。
	 */
	List<SavingGoal> findByUserOrderByStartDateDesc(User user);

	/**
	 * 2. 透過帳戶 ID 找回對應的目標 當你呼叫同學的轉帳功能後，如果想確認這筆錢影響了哪個目標，可以用這個。
	 */
	Optional<SavingGoal> findByAccount(Account account);

	/**
	 * 3. 防呆邏輯：檢查該帳戶是否已被其他目標綁定 在新增 (Create) 目標時，必須先調用這個檢查。 如果回傳
	 * true，代表該帳戶已經在存別的東西了，不能重複綁定。
	 */
	boolean existsByAccountAndStatus(Account account, String status);

	/**
	 * 4. 搜尋特定狀態的目標 例如：只想看 ACTIVE（進行中）或 COMPLETED（已達成）的目標。
	 */
	List<SavingGoal> findByUserAndStatus(User user, String status);
}
