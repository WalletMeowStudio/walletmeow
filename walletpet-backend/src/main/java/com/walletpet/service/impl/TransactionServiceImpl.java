package com.walletpet.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.walletpet.dto.transaction.TransactionCreateRequest;
import com.walletpet.dto.transaction.TransactionResponse;
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

    /*
     * 取得新增 / 編輯交易表單需要的帳戶與分類資料。
     *
     * DTO 最小化後，不再回傳 TransactionFormMetaResponse。
     * 改用 Map：
     *
     * {
     *   "accounts": [...],
     *   "categories": [...]
     * }
     */
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getFormMeta(
            String currentUserId,
            TransactionType transactionType
    ) {
        validateCurrentUserId(currentUserId);

        List<Account> accounts = accountRepository
                .findByUser_UserIdAndIsDeletedFalse(currentUserId)
                .stream()
                // 一般收入 / 支出不顯示存錢目標專用帳戶
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

        List<Map<String, Object>> accountOptions = accounts.stream()
                .map(this::toAccountOptionMap)
                .collect(Collectors.toList());

        List<Map<String, Object>> categoryOptions = categories.stream()
                .map(this::toCategoryOptionMap)
                .collect(Collectors.toList());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("accounts", accountOptions);
        response.put("categories", categoryOptions);

        return response;
    }

    /*
     * 新增交易。
     *
     * 收入：帳戶餘額增加。
     * 支出：帳戶餘額減少。
     *
     * 新增完成後，重新計算該日期的每日記帳獎勵。
     */
    @Override
    public TransactionResponse createTransaction(
            String currentUserId,
            TransactionCreateRequest request
    ) {
        validateCurrentUserId(currentUserId);
        validateCreateRequest(request);

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到該使用者"));

        Account account = accountRepository
                .findByAccountIdAndUser_UserIdAndIsDeletedFalse(
                        request.getAccountId(),
                        currentUserId
                )
                .orElseThrow(() -> new ResourceNotFoundException("帳戶不存在"));

        validateAccountCanUseForTransaction(account);

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
        transaction.setNote(normalizeNullableText(request.getNote()));

        Transaction savedTransaction = transactionRepository.save(transaction);

        /*
         * Account 是 managed entity，通常會自動 flush。
         * 這裡明確 save，方便測試時確認餘額有更新。
         */
        accountRepository.save(account);

        dailyRewardService.handleDailyReward(
                currentUserId,
                savedTransaction.getTransactionDate()
        );

        return TransactionMapper.toResponse(savedTransaction);
    }

    /*
     * 查詢交易明細。
     *
     * DTO 最小化後，不再回傳 TransactionListResponse。
     * 改用 Map：
     *
     * {
     *   "summary": {...},
     *   "items": [...],
     *   "page": 0,
     *   "size": 10,
     *   "totalElements": 30,
     *   "totalPages": 3,
     *   "first": true,
     *   "last": false
     * }
     */
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> searchTransactions(
            String currentUserId,
            LocalDate startDate,
            LocalDate endDate,
            Integer accountId,
            String categoryId,
            TransactionType type,
            int page,
            int size
    ) {
        validateCurrentUserId(currentUserId);
        validateDateRange(startDate, endDate);
        validatePageParameter(page, size);

        /*
         * hidden input 只可用來帶 id，不可作為資料歸屬依據。
         * 所以有 accountId / categoryId 時，仍需驗證是否屬於 currentUserId。
         */
        validateOptionalAccountBelongsToCurrentUser(currentUserId, accountId);
        validateOptionalCategoryBelongsToCurrentUser(currentUserId, categoryId);

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

        /*
         * summary 要統計符合篩選條件的全部資料，不只統計當頁資料。
         */
        List<Transaction> summaryTransactions = transactionRepository.searchTransactionsForSummary(
                currentUserId,
                startDate,
                endDate,
                accountId,
                categoryId,
                type
        );

        Map<String, Object> summary = calculateSummary(summaryTransactions);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("summary", summary);
        response.put("items", items);
        response.put("page", transactionPage.getNumber());
        response.put("size", transactionPage.getSize());
        response.put("totalElements", transactionPage.getTotalElements());
        response.put("totalPages", transactionPage.getTotalPages());
        response.put("first", transactionPage.isFirst());
        response.put("last", transactionPage.isLast());

        return response;
    }

    /*
     * 查詢單筆交易。
     */
    @Override
    @Transactional(readOnly = true)
    public TransactionResponse findById(
            String currentUserId,
            String transactionId
    ) {
        validateCurrentUserId(currentUserId);

        if (!hasText(transactionId)) {
            throw new BusinessException("交易 ID 不可為空");
        }

        Transaction transaction = transactionRepository
                .findByTransactionIdAndUser_UserId(
                        transactionId,
                        currentUserId
                )
                .orElseThrow(() -> new ResourceNotFoundException("交易不存在"));

        return TransactionMapper.toResponse(transaction);
    }

    /*
     * 修改交易。
     *
     * 修改邏輯：
     * 1. 記住舊交易日期
     * 2. 還原舊交易對舊帳戶餘額造成的影響
     * 3. 查詢新帳戶與新分類
     * 4. 套用新交易對新帳戶餘額造成的影響
     * 5. 儲存交易
     * 6. 重新計算每日任務
     */
    @Override
    public TransactionResponse updateTransaction(
            String currentUserId,
            String transactionId,
            TransactionUpdateRequest request
    ) {
        validateCurrentUserId(currentUserId);

        if (!hasText(transactionId)) {
            throw new BusinessException("交易 ID 不可為空");
        }

        validateUpdateRequest(request);

        Transaction transaction = transactionRepository
                .findByTransactionIdAndUser_UserId(
                        transactionId,
                        currentUserId
                )
                .orElseThrow(() -> new ResourceNotFoundException("交易不存在"));

        LocalDate oldTransactionDate = transaction.getTransactionDate();
        Account oldAccount = transaction.getAccount();

        restoreAmountToAccount(
                oldAccount,
                transaction.getTransactionType(),
                transaction.getTransactionAmount()
        );

        Account newAccount = accountRepository
                .findByAccountIdAndUser_UserIdAndIsDeletedFalse(
                        request.getAccountId(),
                        currentUserId
                )
                .orElseThrow(() -> new ResourceNotFoundException("帳戶不存在"));

        validateAccountCanUseForTransaction(newAccount);

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

        applyAmountToAccount(
                newAccount,
                request.getTransactionType(),
                request.getTransactionAmount()
        );

        transaction.setAccount(newAccount);
        transaction.setCategory(newCategory);
        transaction.setTransactionType(request.getTransactionType());
        transaction.setTransactionAmount(request.getTransactionAmount());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setNote(normalizeNullableText(request.getNote()));

        Transaction savedTransaction = transactionRepository.save(transaction);

        accountRepository.save(oldAccount);

        if (!oldAccount.getAccountId().equals(newAccount.getAccountId())) {
            accountRepository.save(newAccount);
        }

        /*
         * 修改交易後要重算每日記帳獎勵。
         *
         * 如果日期沒變，只重算同一天。
         * 如果日期有變，舊日期和新日期都要重算。
         */
        dailyRewardService.handleDailyReward(
                currentUserId,
                oldTransactionDate
        );

        if (!oldTransactionDate.equals(savedTransaction.getTransactionDate())) {
            dailyRewardService.handleDailyReward(
                    currentUserId,
                    savedTransaction.getTransactionDate()
            );
        }

        return TransactionMapper.toResponse(savedTransaction);
    }

    /*
     * 刪除交易。
     *
     * 刪除前先還原帳戶餘額。
     * 刪除後重新計算該日期的每日任務。
     */
    @Override
    public TransactionResponse deleteTransaction(
            String currentUserId,
            String transactionId
    ) {
        validateCurrentUserId(currentUserId);

        if (!hasText(transactionId)) {
            throw new BusinessException("交易 ID 不可為空");
        }

        Transaction transaction = transactionRepository
                .findByTransactionIdAndUser_UserId(
                        transactionId,
                        currentUserId
                )
                .orElseThrow(() -> new ResourceNotFoundException("交易不存在"));

        LocalDate deletedTransactionDate = transaction.getTransactionDate();
        Account account = transaction.getAccount();

        restoreAmountToAccount(
                account,
                transaction.getTransactionType(),
                transaction.getTransactionAmount()
        );

        TransactionResponse response = TransactionMapper.toResponse(transaction);

        transactionRepository.delete(transaction);
        accountRepository.save(account);

        dailyRewardService.handleDailyReward(
                currentUserId,
                deletedTransactionDate
        );

        return response;
    }

    /*
     * 查詢交易摘要。
     *
     * DTO 最小化後，不再回傳 TransactionSummaryResponse。
     * 改用 Map：
     *
     * {
     *   "totalIncome": 10000,
     *   "totalExpense": 3000,
     *   "balance": 7000,
     *   "transactionCount": 5
     * }
     */
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getSummary(
            String currentUserId,
            LocalDate startDate,
            LocalDate endDate,
            Integer accountId,
            String categoryId
    ) {
        validateCurrentUserId(currentUserId);
        validateDateRange(startDate, endDate);
        validateOptionalAccountBelongsToCurrentUser(currentUserId, accountId);
        validateOptionalCategoryBelongsToCurrentUser(currentUserId, categoryId);

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

    /*
     * 每日任務用：
     * 計算某使用者某一天的收入 / 支出交易筆數。
     *
     * 注意：
     * DailyRewardServiceImpl 目前已直接使用 TransactionRepository，
     * 所以這個方法可保留給其他模組使用，但 DailyReward 不再透過它呼叫，
     * 以避免循環依賴。
     */
    @Override
    @Transactional(readOnly = true)
    public int countDailyBookkeepingTransactions(
            String currentUserId,
            LocalDate transactionDate
    ) {
        validateCurrentUserId(currentUserId);

        if (transactionDate == null) {
            throw new BusinessException("交易日期不可為空");
        }

        return transactionRepository.countByUser_UserIdAndTransactionDateAndTransactionTypeIn(
                currentUserId,
                transactionDate,
                Arrays.asList(TransactionType.INCOME, TransactionType.EXPENSE)
        );
    }

    private void validateCreateRequest(TransactionCreateRequest request) {
        if (request == null) {
            throw new BusinessException("交易資料不可為空");
        }

        validateTransactionFields(
                request.getTransactionType(),
                request.getAccountId(),
                request.getCategoryId(),
                request.getTransactionAmount(),
                request.getTransactionDate()
        );
    }

    private void validateUpdateRequest(TransactionUpdateRequest request) {
        if (request == null) {
            throw new BusinessException("交易資料不可為空");
        }

        validateTransactionFields(
                request.getTransactionType(),
                request.getAccountId(),
                request.getCategoryId(),
                request.getTransactionAmount(),
                request.getTransactionDate()
        );
    }

    private void validateTransactionFields(
            TransactionType transactionType,
            Integer accountId,
            String categoryId,
            BigDecimal transactionAmount,
            LocalDate transactionDate
    ) {
        if (transactionType == null) {
            throw new BusinessException("交易類型不可為空");
        }

        if (accountId == null) {
            throw new BusinessException("帳戶不可為空");
        }

        if (!hasText(categoryId)) {
            throw new BusinessException("分類不可為空");
        }

        if (transactionAmount == null
                || transactionAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("交易金額必須大於 0");
        }

        if (transactionDate == null) {
            throw new BusinessException("交易日期不可為空");
        }
    }

    private void validateCurrentUserId(String currentUserId) {
        if (!hasText(currentUserId)) {
            throw new BusinessException("使用者不可為空");
        }
    }

    private void validateDateRange(
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BusinessException("開始日期不可晚於結束日期");
        }
    }

    private void validatePageParameter(
            int page,
            int size
    ) {
        if (page < 0) {
            throw new BusinessException("頁碼不可小於 0");
        }

        if (size <= 0) {
            throw new BusinessException("每頁筆數必須大於 0");
        }

        if (size > 100) {
            throw new BusinessException("每頁筆數不可超過 100");
        }
    }

    private void validateOptionalAccountBelongsToCurrentUser(
            String currentUserId,
            Integer accountId
    ) {
        if (accountId == null) {
            return;
        }

        accountRepository.findByAccountIdAndUser_UserId(
                accountId,
                currentUserId
        ).orElseThrow(() -> new ResourceNotFoundException("帳戶不存在"));
    }

    private void validateOptionalCategoryBelongsToCurrentUser(
            String currentUserId,
            String categoryId
    ) {
        if (!hasText(categoryId)) {
            return;
        }

        categoryRepository.findByCategoryIdAndUser_UserId(
                categoryId,
                currentUserId
        ).orElseThrow(() -> new ResourceNotFoundException("分類不存在"));
    }

    private void validateAccountCanUseForTransaction(Account account) {
        if (account == null) {
            throw new BusinessException("帳戶資料異常");
        }

        if (Boolean.TRUE.equals(account.getIsDeleted())) {
            throw new BusinessException("此帳戶已停用，不能新增或修改交易");
        }

        if (Boolean.TRUE.equals(account.getIsSavingAccount())) {
            throw new BusinessException("存錢目標專用帳戶不可直接新增收入或支出交易");
        }
    }

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
        } else {
            throw new BusinessException("不支援的交易類型");
        }
    }

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
        } else {
            throw new BusinessException("不支援的交易類型");
        }
    }

    private Map<String, Object> calculateSummary(List<Transaction> transactions) {
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        for (Transaction transaction : transactions) {
            if (TransactionType.INCOME.equals(transaction.getTransactionType())) {
                totalIncome = totalIncome.add(transaction.getTransactionAmount());
            } else if (TransactionType.EXPENSE.equals(transaction.getTransactionType())) {
                totalExpense = totalExpense.add(transaction.getTransactionAmount());
            }
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalIncome", totalIncome);
        summary.put("totalExpense", totalExpense);
        summary.put("balance", totalIncome.subtract(totalExpense));
        summary.put("transactionCount", transactions.size());

        return summary;
    }

    private Map<String, Object> toAccountOptionMap(Account account) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("accountId", account.getAccountId());
        map.put("accountName", account.getAccountName());
        map.put("balance", account.getBalance());
        map.put("isSavingAccount", account.getIsSavingAccount());

        return map;
    }

    private Map<String, Object> toCategoryOptionMap(Category category) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("categoryId", category.getCategoryId());
        map.put("categoryName", category.getCategoryName());
        map.put("categoryType", category.getCategoryType());
        map.put("icon", category.getIcon());
        map.put("color", category.getColor());

        return map;
    }

    private String normalizeNullableText(String value) {
        if (value == null) {
            return null;
        }

        String trimmedValue = value.trim();

        if (trimmedValue.isEmpty()) {
            return null;
        }

        return trimmedValue;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}