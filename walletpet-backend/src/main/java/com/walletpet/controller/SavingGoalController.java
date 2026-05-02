package com.walletpet.controller;

import com.walletpet.dto.savinggoal.SavingGoalRequest;
import com.walletpet.dto.common.ApiResponse;
import com.walletpet.entity.SavingGoal;
import com.walletpet.security.CurrentUserUtil;
import com.walletpet.service.SavingGoalService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/saving-goals")
@RequiredArgsConstructor
public class SavingGoalController {

	private final SavingGoalService savingGoalService;
	private final CurrentUserUtil currentUserUtil;

	/**
	 * 1. 建立存款目標 POST /api/saving-goals
	 */
	@PostMapping
	public ApiResponse<SavingGoal> createGoal(@RequestBody SavingGoalRequest request) {
		String userId = currentUserUtil.getCurrentUserId();
		SavingGoal data = savingGoalService.createSavingGoal(userId, request);
		return ApiResponse.success("存款目標建立成功", data);
	}

	/**
	 * 2. 執行存款動作 (借用轉帳邏輯) POST
	 * /api/saving-goals/{id}/deposit?amount=500&fromAccountId=1
	 */
	@PostMapping("/{id}/deposit")
	public ApiResponse<Void> deposit(@PathVariable String id, @RequestParam BigDecimal amount,
			@RequestParam Integer fromAccountId) {

		String userId = currentUserUtil.getCurrentUserId();
		savingGoalService.deposit(userId, id, amount, fromAccountId);
		return ApiResponse.success("存款成功", null);
	}

	/**
	 * 3. 達成目標 (觸發快照 + 帳戶解鎖) PUT /api/saving-goals/{id}/complete
	 */
	@PutMapping("/{id}/complete")
	public ApiResponse<SavingGoal> completeGoal(@PathVariable String id) {
		String userId = currentUserUtil.getCurrentUserId();
		SavingGoal data = savingGoalService.completeGoal(userId, id);
		return ApiResponse.success("恭喜達成目標！該帳戶已轉為一般帳戶。", data);
	}

	/**
	 * 4. 放棄並刪除目標 (強制清算 + 停用帳戶 + 硬刪除) DELETE /api/saving-goals/{id}?toAccountId=2
	 * 這裡要傳入「錢要轉去哪裡」的帳戶 ID
	 */
	@DeleteMapping("/{id}")
	public ApiResponse<Void> deleteGoal(@PathVariable String id, @RequestParam Integer toAccountId) {

		String userId = currentUserUtil.getCurrentUserId();
		savingGoalService.deleteGoal(userId, id, toAccountId);
		return ApiResponse.success("目標已刪除，餘額已全數轉出並停用原帳戶。", null);
	}

	/**
	 * 5. 查詢目前登入者的所有目標 GET /api/saving-goals
	 */
	@GetMapping
	public ApiResponse<List<SavingGoal>> findAll() {
		// 這邊你可以直接呼叫 Repository 或透過 Service 封裝
		String userId = currentUserUtil.getCurrentUserId();
		List<SavingGoal> data = savingGoalService.findAll(userId);
		return ApiResponse.success("查詢成功", data);
	}
	
	/**
	 * 6. 修改目標 (名稱與金額) 
	 * PUT /api/saving-goals/{id}
	 */
	@PutMapping("/{id}")
	public ApiResponse<SavingGoal> updateGoal(@PathVariable String id, @RequestBody SavingGoalRequest request) {
	    String userId = currentUserUtil.getCurrentUserId();
	    // 呼叫你剛發現的那個 Service 方法
	    SavingGoal data = savingGoalService.updateGoal(userId, id, request);
	    return ApiResponse.success("目標修改成功", data);
	}
}