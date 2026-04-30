package com.walletpet.service.impl;

import com.walletpet.dto.savinggoal.SavingGoalRequest;
import com.walletpet.dto.transfer.TransferCreateRequest;
import com.walletpet.entity.Account;
import com.walletpet.entity.SavingGoal;
import com.walletpet.entity.User;
import com.walletpet.exception.BusinessException;
import com.walletpet.exception.ResourceNotFoundException;
import com.walletpet.repository.AccountRepository;
import com.walletpet.repository.SavingGoalRepository;
import com.walletpet.service.AccountService;
import com.walletpet.service.SavingGoalService;
import com.walletpet.service.TransferService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class SavingGoalServiceImpl implements SavingGoalService {

	private final SavingGoalRepository savingGoalRepository;
	private final AccountRepository accountRepository;
	private final TransferService transferService; // 借用同學的轉帳功能
	private final UserServiceImpl userServiceImpl;
	private final AccountService accountService;

	/**
	 * 新增存款目標
	 */
	@Override
	public SavingGoal createSavingGoal(String currentUserId, SavingGoalRequest request) {
		// 1. 取得帳戶並驗證
		Account account = accountRepository.findByAccountIdAndUser_UserId(request.getAccountId(), currentUserId)
				.orElseThrow(() -> new ResourceNotFoundException("找不到該帳戶"));

		// 2. 邏輯鎖死：驗證是否為存款帳戶
		if (!Boolean.TRUE.equals(account.getIsSavingAccount())) {
			throw new BusinessException("該帳戶未標記為存款帳戶，請先在帳戶頁面設定。");
		}

		// 3. 防呆：檢查該帳戶是否已被「進行中」的目標綁定
		if (savingGoalRepository.existsByAccountAndStatus(account, "ACTIVE")) {
			throw new BusinessException("該帳戶已經有一個進行中的存款目標。");
		}

		User user = userServiceImpl.getUserEntityById(currentUserId);

		// 4. 建立目標
		SavingGoal goal = new SavingGoal();
		goal.setSavingGoalid(UUID.randomUUID().toString()); // 生成唯一 ID
		goal.setGoalName(request.getGoalName());
		goal.setTargetAmount(request.getTargetAmount());
		goal.setStartDate(request.getStartDate());
		goal.setEndDate(request.getEndDate());
		goal.setUser(user);
		goal.setAccount(account);
		goal.setStatus("ACTIVE");

		return savingGoalRepository.save(goal);
	}

	/**
	 * 存款動作：直接借用 TransferService
	 */
	@Override
	public void deposit(String currentUserId, String goalId, BigDecimal amount, Integer fromAccountId) {
		SavingGoal goal = savingGoalRepository.findById(goalId)
				.orElseThrow(() -> new ResourceNotFoundException("找不到目標"));

		if (!"ACTIVE".equals(goal.getStatus())) {
			throw new BusinessException("只有進行中的目標可以存款。");
		}

		// 組裝轉帳請求
		TransferCreateRequest transferRequest = new TransferCreateRequest();
		transferRequest.setFromAccountId(fromAccountId); // 來源：User 選的普通帳戶
		transferRequest.setToAccountId(goal.getAccount().getAccountId()); // 目標：綁定的存款帳戶
		transferRequest.setTransactionAmount(amount);
		transferRequest.setTransactionDate(LocalDate.now());
		transferRequest.setNote("存款目標：" + goal.getGoalName());

		// 呼叫同學的轉帳服務
		transferService.createTransfer(currentUserId, transferRequest);

		// 邏輯提示：這裡不用更新 SavingGoal，因為顯示時直接抓 account.getBalance() 即可
	}

	@Override
	public SavingGoal completeGoal(String currentUserId, String goalId) {
		SavingGoal goal = savingGoalRepository.findById(goalId)
				.orElseThrow(() -> new ResourceNotFoundException("找不到目標"));

		// 1. 執行快照
		goal.setFinalAmount(goal.getAccount().getBalance());
		goal.setFinalAccountName(goal.getAccount().getAccountName());
		goal.setStatus("COMPLETED");

		// 2. 解除存款帳戶鎖定：將 isSavingAccount 轉為 false
		Account account = goal.getAccount();
		account.setIsSavingAccount(false);
		accountRepository.save(account);

		return savingGoalRepository.save(goal);
	}

	/**
	 * 修改目標：僅限金額與名稱
	 */
	@Override
	public SavingGoal updateGoal(String currentUserId, String goalId, SavingGoalRequest request) {
		SavingGoal goal = savingGoalRepository.findById(goalId)
				.orElseThrow(() -> new ResourceNotFoundException("找不到目標"));

		goal.setGoalName(request.getGoalName());
		goal.setTargetAmount(request.getTargetAmount());

		return savingGoalRepository.save(goal);
	}

	/**
	 * 場景 2：刪除/放棄目標（半途而廢） 動作：強制轉帳出清 -> 停用帳戶 -> 硬刪除目標
	 * 
	 * @param toAccountId 錢要轉去哪裡的目標帳戶 ID
	 */
	@Override
	public void deleteGoal(String currentUserId, String goalId, Integer toAccountId) {
		SavingGoal goal = savingGoalRepository.findById(goalId)
				.orElseThrow(() -> new ResourceNotFoundException("找不到目標"));

		Account fromAccount = goal.getAccount();
		BigDecimal currentBalance = fromAccount.getBalance();

		// 1. 強行要求轉出來：只要有錢，就啟動轉帳流程
		if (currentBalance.compareTo(BigDecimal.ZERO) > 0) {
			// 驗證目標帳戶（不能是負債帳戶邏輯應在 transferService 或此處檢查）
			Account toAccount = accountRepository.findById(toAccountId)
					.orElseThrow(() -> new ResourceNotFoundException("請選擇正確的收款帳戶"));

			if (Boolean.TRUE.equals(toAccount.getIsLiability())) {
				throw new BusinessException("不能將存款轉入負債帳戶（如信用卡），請選擇現金或銀行帳戶。");
			}

			// 執行轉帳：金額要是目前帳戶的「全部金額」
			TransferCreateRequest request = new TransferCreateRequest();
			request.setFromAccountId(fromAccount.getAccountId());
			request.setToAccountId(toAccountId);
			request.setTransactionAmount(currentBalance);
			request.setNote("放棄目標「" + goal.getGoalName() + "」，餘額全數轉出。");
			request.setTransactionDate(LocalDate.now());

			transferService.createTransfer(currentUserId, request);
		}

		// 2. 停用該存款帳戶（呼叫同學的功能）
		accountService.disableAccount(currentUserId, fromAccount.getAccountId());

		// 3. 硬刪除存款目標
		savingGoalRepository.delete(goal);
	}

	@Override
	public List<SavingGoal> findAll(String currentUserId) {
		User user = userServiceImpl.getUserEntityById(currentUserId);
		return savingGoalRepository.findByUserOrderByStartDateDesc(user);
	}
}