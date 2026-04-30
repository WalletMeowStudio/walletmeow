package com.walletpet.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.walletpet.dto.transaction.TransactionCreateRequest;
import com.walletpet.dto.transaction.TransactionFormMetaResponse;
import com.walletpet.dto.transaction.TransactionListResponse;
import com.walletpet.dto.transaction.TransactionResponse;
import com.walletpet.dto.transaction.TransactionSummaryResponse;
import com.walletpet.dto.transaction.TransactionUpdateRequest;
import com.walletpet.entity.Account;
import com.walletpet.entity.Category;
import com.walletpet.entity.Transaction;
import com.walletpet.entity.User;
import com.walletpet.enums.CategoryType;
import com.walletpet.enums.TransactionType;
import com.walletpet.exception.BusinessException;
import com.walletpet.exception.ResourceNotFoundException;
import com.walletpet.mapper.TransactionMapper;
import com.walletpet.repository.AccountRepository;
import com.walletpet.repository.CategoryRepository;
import com.walletpet.repository.TransactionRepository;
import com.walletpet.repository.UserRepository;
import com.walletpet.service.DailyRewardService;
import com.walletpet.service.TransactionService;
import com.walletpet.util.IdGenerator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    private final UserRepository userRepository;

    private final AccountRepository accountRepository;

    private final CategoryRepository categoryRepository;

    private final DailyRewardService dailyRewardService;

    /**
     * 取得新增 / 編輯交易表單需要的帳戶與分類資料。
     */
    @Override
    @Transactional(readOnly = true)
    public TransactionFormMetaResponse getFormMeta(
            String currentUserId,
            TransactionType transactionType
    ) {
        List<Account> accounts = accountRepository.findByUser_UserIdAndIsDeletedFalse(currentUserId)
                .stream()
                // 一般收入 / 支出不顯示存款目標專用帳戶
                .filter(account -> !Boolean.TRUE.equals(account.getIsSavingAccount()))
                .collect(Collectors.toList());

        CategoryType categoryType = null;

        if (transactionType != null) {
            categoryType = CategoryType.valueOf(transactionType.name());
        }

        List<Category> categories;

        if (categoryType == null) {
            categories = categoryRepository.findByUser_UserIdAndIsDisableFalse(currentUserId);
        } else {
            categories = categoryRepository.findByUser_UserIdAndCategoryTypeAndIsDisableFalse(
                    currentUserId,
                    categoryType
            );
        }

        TransactionFormMetaResponse response = new TransactionFormMetaResponse();

        response.setAccounts(
                accounts.stream()
                        .map(account -> {
                            TransactionFormMetaResponse.AccountOption option =
                                    new TransactionFormMetaResponse.AccountOption();

                            option.setAccountId(account.getAccountId());
                            option.setAccountName(account.getAccountName());
                            option.setBalance(account.getBalance());
                            option.setIsSavingAccount(account.getIsSavingAccount());

                            return option;
                        })
                        .collect(Collectors.toList())
        );

        response.setCategories(
                categories.stream()
                        .map(category -> {
                            TransactionFormMetaResponse.CategoryOption option =
                                    new TransactionFormMetaResponse.CategoryOption();

                            option.setCategoryId(category.getCategoryId());
                            option.setCategoryName(category.getCategoryName());
                            option.setCategoryType(category.getCategoryType());
                            option.setIcon(category.getIcon());
                            option.setColor(category.getColor());

                            return option;
                        })
                        .collect(Collectors.toList())
        );

        return response;
    }

    /**
     * 新增交易。
     *
     * 收入：帳戶餘額增加
     * 支出：帳戶餘額減少
     *
     * 新增完成後會重新計算該日期的每日記帳獎勵。
     */
    @Override
    public TransactionResponse createTransaction(
            String currentUserId,
            TransactionCreateRequest request
    ) {
        validateCreateRequest(request);

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到該使用者"));

        Account account = accountRepository
                .findByAccountIdAndUser_UserIdAndIsDeletedFalse(
                        request.getAccountId(),
                        currentUserId
                )
                .orElseThrow(() -> new ResourceNotFoundException("帳戶不存在"));

        Category category = categoryRepository
                .findByCategoryIdAndUser_UserId(
                        request.getCategoryId(),
                        currentUserId
                )
                .orElseThrow(() -> new ResourceNotFoundException("分類不存在"));

        validateCategoryMatchTransactionType(
                request.getTransactionType(),
                category
        );

        applyAmountToAccount(
                account,
                request.getTransactionType(),
                request.getTransactionAmount()
        );

        Transaction transaction = new Transaction();
        transaction.setTransactionId(IdGenerator.generate("TXN"));
        transaction.setUser(user);
        transaction.setAccount(account);
        transaction.setCategory(category);
        transaction.setTransactionType(request.getTransactionType());
        transaction.setTransactionAmount(request.getTransactionAmount());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setNote(request.getNote());

        Transaction savedTransaction = transactionRepository.save(transaction);

        // 新增交易後，重新計算該日期的每日記帳獎勵
        dailyRewardService.handleDailyReward(
                currentUserId,
                savedTransaction.getTransactionDate()
        );

        return TransactionMapper.toResponse(savedTransaction);
    }

    /**
     * 依條件查詢交易。
     */
    @Override
    @Transactional(readOnly = true)
    public TransactionListResponse searchTransactions(
            String currentUserId,
            LocalDate startDate,
            LocalDate endDate,
            Integer accountId,
            String categoryId,
            TransactionType type,
            int page,
            int size
    ) {
        if (page < 0) {
            throw new BusinessException("頁碼不可小於 0");
        }

        if (size <= 0) {
            throw new BusinessException("每頁筆數必須大於 0");
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Order.desc("transactionDate"),
                        Sort.Order.desc("createdAt")
                )
        );

        Page<Transaction> transactionPage = transactionRepository.searchTransactions(
                currentUserId,
                startDate,
                endDate,
                accountId,
                categoryId,
                type,
                pageable
        );

        List<TransactionResponse> items = transactionPage.getContent()
                .stream()
                .map(TransactionMapper::toResponse)
                .collect(Collectors.toList());

        List<Transaction> summaryTransactions = transactionRepository.searchTransactionsForSummary(
                currentUserId,
                startDate,
                endDate,
                accountId,
                categoryId,
                type
        );

        TransactionSummaryResponse summary = calculateSummary(summaryTransactions);

        TransactionListResponse response = new TransactionListResponse();
        response.setSummary(summary);
        response.setItems(items);
        response.setPage(transactionPage.getNumber());
        response.setSize(transactionPage.getSize());
        response.setTotalElements(transactionPage.getTotalElements());
        response.setTotalPages(transactionPage.getTotalPages());
        response.setFirst(transactionPage.isFirst());
        response.setLast(transactionPage.isLast());

        return response;
    }

    /**
     * 查詢單筆交易。
     */
    @Override
    @Transactional(readOnly = true)
    public TransactionResponse findById(
            String currentUserId,
            String transactionId
    ) {
        Transaction transaction = transactionRepository
                .findByTransactionIdAndUser_UserId(
                        transactionId,
                        currentUserId
                )
                .orElseThrow(() -> new ResourceNotFoundException("交易不存在"));

        return TransactionMapper.toResponse(transaction);
    }

    /**
     * 修改交易。
     *
     * 修改邏輯：
     * 1. 先記住舊交易日期
     * 2. 還原舊交易對帳戶餘額的影響
     * 3. 套用新交易對帳戶餘額的影響
     * 4. 儲存交易
     * 5. 重新計算舊日期與新日期的每日獎勵
     */
    @Override
    public TransactionResponse updateTransaction(
            String currentUserId,
            String transactionId,
            TransactionUpdateRequest request
    ) {
        validateUpdateRequest(request);

        Transaction transaction = transactionRepository
                .findByTransactionIdAndUser_UserId(
                        transactionId,
                        currentUserId
                )
                .orElseThrow(() -> new ResourceNotFoundException("交易不存在"));

        LocalDate oldTransactionDate = transaction.getTransactionDate();

        // 1. 先還原舊交易對舊帳戶造成的影響
        restoreAmountToAccount(
                transaction.getAccount(),
                transaction.getTransactionType(),
                transaction.getTransactionAmount()
        );

        // 2. 查詢新的帳戶
        Account newAccount = accountRepository
                .findByAccountIdAndUser_UserIdAndIsDeletedFalse(
                        request.getAccountId(),
                        currentUserId
                )
                .orElseThrow(() -> new ResourceNotFoundException("帳戶不存在"));

        // 3. 查詢新的分類
        Category newCategory = categoryRepository
                .findByCategoryIdAndUser_UserId(
                        request.getCategoryId(),
                        currentUserId
                )
                .orElseThrow(() -> new ResourceNotFoundException("分類不存在"));

        validateCategoryMatchTransactionType(
                request.getTransactionType(),
                newCategory
        );

        // 4. 套用新交易對新帳戶造成的影響
        applyAmountToAccount(
                newAccount,
                request.getTransactionType(),
                request.getTransactionAmount()
        );

        // 5. 更新交易資料
        transaction.setAccount(newAccount);
        transaction.setCategory(newCategory);
        transaction.setTransactionType(request.getTransactionType());
        transaction.setTransactionAmount(request.getTransactionAmount());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setNote(request.getNote());

        Transaction savedTransaction = transactionRepository.save(transaction);

        // 6. 修改交易後，重新計算舊日期的每日記帳獎勵
        dailyRewardService.handleDailyReward(
                currentUserId,
                oldTransactionDate
        );

        // 7. 如果交易日期有改變，也重新計算新日期的每日記帳獎勵
        if (!oldTransactionDate.equals(savedTransaction.getTransactionDate())) {
            dailyRewardService.handleDailyReward(
                    currentUserId,
                    savedTransaction.getTransactionDate()
            );
        }

        return TransactionMapper.toResponse(savedTransaction);
    }

    /**
     * 刪除交易。
     *
     * 刪除前會先還原帳戶餘額。
     * 刪除後會重新計算該日期的每日記帳獎勵。
     */
    @Override
    public TransactionResponse deleteTransaction(
            String currentUserId,
            String transactionId
    ) {
        Transaction transaction = transactionRepository
                .findByTransactionIdAndUser_UserId(
                        transactionId,
                        currentUserId
                )
                .orElseThrow(() -> new ResourceNotFoundException("交易不存在"));

        LocalDate deletedTransactionDate = transaction.getTransactionDate();

        // 1. 刪除交易前，先還原帳戶餘額
        restoreAmountToAccount(
                transaction.getAccount(),
                transaction.getTransactionType(),
                transaction.getTransactionAmount()
        );

        TransactionResponse response = TransactionMapper.toResponse(transaction);

        // 2. 刪除交易
        transactionRepository.delete(transaction);

        // 3. 刪除交易後，重新計算該日期的每日記帳獎勵
        dailyRewardService.handleDailyReward(
                currentUserId,
                deletedTransactionDate
        );

        return response;
    }

    /**
     * 查詢交易摘要。
     */
    @Override
    @Transactional(readOnly = true)
    public TransactionSummaryResponse getSummary(
            String currentUserId,
            LocalDate startDate,
            LocalDate endDate,
            Integer accountId,
            String categoryId
    ) {
        List<Transaction> transactions = transactionRepository.searchTransactionsForSummary(
                currentUserId,
                startDate,
                endDate,
                accountId,
                categoryId,
                null
        );

        return calculateSummary(transactions);
    }

    /**
     * 計算某使用者某一天的收入 / 支出交易筆數。
     *
     * 此方法給 DailyRewardService 使用。
     * 若你目前 DailyRewardServiceImpl 是直接使用 TransactionRepository，
     * 這個方法可保留但不是必須。
     */
    @Override
    @Transactional(readOnly = true)
    public int countDailyBookkeepingTransactions(
            String currentUserId,
            LocalDate transactionDate
    ) {
        if (currentUserId == null || currentUserId.isBlank()) {
            throw new BusinessException("使用者不可為空");
        }

        if (transactionDate == null) {
            throw new BusinessException("交易日期不可為空");
        }

        return transactionRepository.countByUser_UserIdAndTransactionDateAndTransactionTypeIn(
                currentUserId,
                transactionDate,
                java.util.Arrays.asList(TransactionType.INCOME, TransactionType.EXPENSE)
        );
    }

    /**
     * 驗證新增交易資料。
     */
    private void validateCreateRequest(TransactionCreateRequest request) {
        if (request == null) {
            throw new BusinessException("交易資料不可為空");
        }

        if (request.getTransactionType() == null) {
            throw new BusinessException("交易類型不可為空");
        }

        if (request.getAccountId() == null) {
            throw new BusinessException("帳戶不可為空");
        }

        if (request.getCategoryId() == null || request.getCategoryId().isBlank()) {
            throw new BusinessException("分類不可為空");
        }

        if (request.getTransactionAmount() == null
                || request.getTransactionAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("交易金額必須大於 0");
        }

        if (request.getTransactionDate() == null) {
            throw new BusinessException("交易日期不可為空");
        }
    }

    /**
     * 驗證修改交易資料。
     */
    private void validateUpdateRequest(TransactionUpdateRequest request) {
        if (request == null) {
            throw new BusinessException("交易資料不可為空");
        }

        if (request.getTransactionType() == null) {
            throw new BusinessException("交易類型不可為空");
        }

        if (request.getAccountId() == null) {
            throw new BusinessException("帳戶不可為空");
        }

        if (request.getCategoryId() == null || request.getCategoryId().isBlank()) {
            throw new BusinessException("分類不可為空");
        }

        if (request.getTransactionAmount() == null
                || request.getTransactionAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("交易金額必須大於 0");
        }

        if (request.getTransactionDate() == null) {
            throw new BusinessException("交易日期不可為空");
        }
    }

    /**
     * 檢查交易類型與分類類型是否一致。
     *
     * 例如：
     * EXPENSE 交易只能使用 EXPENSE 分類。
     * INCOME 交易只能使用 INCOME 分類。
     */
    private void validateCategoryMatchTransactionType(
            TransactionType transactionType,
            Category category
    ) {
        if (category == null || category.getCategoryType() == null) {
            throw new BusinessException("分類資料異常");
        }

        if (!transactionType.name().equals(category.getCategoryType().name())) {
            throw new BusinessException("交易類型與分類類型不一致");
        }

        if (Boolean.TRUE.equals(category.getIsDisable())) {
            throw new BusinessException("此分類已停用，不能新增或修改交易");
        }
    }

    /**
     * 套用交易金額到帳戶。
     *
     * INCOME：balance + amount
     * EXPENSE：balance - amount
     */
    private void applyAmountToAccount(
            Account account,
            TransactionType transactionType,
            BigDecimal amount
    ) {
        if (account.getBalance() == null) {
            account.setBalance(BigDecimal.ZERO);
        }

        if (TransactionType.INCOME.equals(transactionType)) {
            account.setBalance(account.getBalance().add(amount));
        } else if (TransactionType.EXPENSE.equals(transactionType)) {
            account.setBalance(account.getBalance().subtract(amount));
        }
    }

    /**
     * 還原交易金額對帳戶造成的影響。
     *
     * 原本是收入：刪除 / 修改時要扣回
     * 原本是支出：刪除 / 修改時要加回
     */
    private void restoreAmountToAccount(
            Account account,
            TransactionType transactionType,
            BigDecimal amount
    ) {
        if (account.getBalance() == null) {
            account.setBalance(BigDecimal.ZERO);
        }

        if (TransactionType.INCOME.equals(transactionType)) {
            account.setBalance(account.getBalance().subtract(amount));
        } else if (TransactionType.EXPENSE.equals(transactionType)) {
            account.setBalance(account.getBalance().add(amount));
        }
    }

    /**
     * 計算查詢結果摘要。
     */
    private TransactionSummaryResponse calculateSummary(List<Transaction> transactions) {
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        for (Transaction transaction : transactions) {
            if (TransactionType.INCOME.equals(transaction.getTransactionType())) {
                totalIncome = totalIncome.add(transaction.getTransactionAmount());
            } else if (TransactionType.EXPENSE.equals(transaction.getTransactionType())) {
                totalExpense = totalExpense.add(transaction.getTransactionAmount());
            }
        }

        TransactionSummaryResponse response = new TransactionSummaryResponse();
        response.setTotalIncome(totalIncome);
        response.setTotalExpense(totalExpense);
        response.setBalance(totalIncome.subtract(totalExpense));
        response.setTransactionCount(transactions.size());

        return response;
    }
}