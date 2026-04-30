package com.walletpet.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.walletpet.dto.account.AccountCreateRequest;
import com.walletpet.dto.account.AccountResponse;
import com.walletpet.dto.account.AccountSummaryResponse;
import com.walletpet.dto.account.AccountUpdateRequest;
import com.walletpet.entity.Account;
import com.walletpet.entity.User;
import com.walletpet.exception.BusinessException;
import com.walletpet.exception.ResourceNotFoundException;
import com.walletpet.mapper.AccountMapper;
import com.walletpet.repository.AccountRepository;
import com.walletpet.repository.UserRepository;
import com.walletpet.service.AccountService;

import lombok.RequiredArgsConstructor;

/**
 * AccountServiceImpl 是帳戶模組的商業邏輯實作。
 *
 * 分層觀念：
 * Controller：接收前端 API 請求。
 * Service：驗證資料、判斷規則、決定要怎麼更新資料。
 * Repository：真的去操作 MySQL 資料表。
 * Entity：對應 accounts 資料表。
 * DTO：整理成前端需要的 JSON 格式。
 */
@Service
@RequiredArgsConstructor // Lombok 會自動產生建構子，Spring 會用建構子注入 Repository。
@Transactional // 預設每個方法都包在交易中，避免資料更新一半失敗。
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    /**
     * 查詢帳戶列表。
     *
     * includeDeleted=false：只顯示正常帳戶，通常給帳戶總覽頁使用。
     * includeDeleted=true：連停用帳戶也查出來，方便後台或測試確認資料。
     */
    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> findAccounts(
            String currentUserId,
            boolean includeDeleted
    ) {
        List<Account> accounts;

        if (includeDeleted) {
            accounts = accountRepository.findByUser_UserId(currentUserId);
        } else {
            accounts = accountRepository.findByUser_UserIdAndIsDeletedFalse(currentUserId);
        }

        return accounts.stream()
                .map(AccountMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 計算帳戶總覽摘要。
     *
     * 規則：
     * 1. 只計算 isDeleted=false 的啟用帳戶。
     * 2. isLiability=false 視為資產帳戶，加入 totalAssets。
     * 3. isLiability=true 視為負債帳戶，加入 totalLiabilities。
     * 4. netWorth = totalAssets - totalLiabilities。
     *
     * 注意：負債通常在前端想看到正數，所以這裡用 balance.abs() 累加。
     * 例如信用卡 balance 是 3000 或 -3000，都會顯示負債 3000。
     */
    @Override
    @Transactional(readOnly = true)
    public AccountSummaryResponse getAccountSummary(String currentUserId) {
        List<Account> accounts = accountRepository.findByUser_UserIdAndIsDeletedFalse(currentUserId);

        BigDecimal totalAssets = BigDecimal.ZERO;
        BigDecimal totalLiabilities = BigDecimal.ZERO;

        for (Account account : accounts) {
            BigDecimal balance = account.getBalance() == null ? BigDecimal.ZERO : account.getBalance();

            if (Boolean.TRUE.equals(account.getIsLiability())) {
                totalLiabilities = totalLiabilities.add(balance.abs());
            } else {
                totalAssets = totalAssets.add(balance);
            }
        }

        BigDecimal netWorth = totalAssets.subtract(totalLiabilities);

        return new AccountSummaryResponse(totalAssets, totalLiabilities, netWorth);
    }

    /** 查詢單一帳戶，必須同時符合 accountId 與 currentUserId，避免查到別人的資料。 */
    @Override
    @Transactional(readOnly = true)
    public AccountResponse findById(
            String currentUserId,
            Integer accountId
    ) {
        Account account = accountRepository
                .findByAccountIdAndUser_UserId(accountId, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("帳戶不存在"));

        return AccountMapper.toResponse(account);
    }

    /**
     * 新增帳戶。
     *
     * initialBalance 是帳戶建立時的初始餘額，會直接寫入 accounts.balance。
     * 後續收入、支出、轉帳會再由 TransactionService / TransferService 更新 balance。
     */
    @Override
    public AccountResponse createAccount(
            String currentUserId,
            AccountCreateRequest request
    ) {
        if (request == null) {
            throw new BusinessException("帳戶資料不可為空");
        }

        if (request.getAccountName() == null || request.getAccountName().isBlank()) {
            throw new BusinessException("帳戶名稱不可為空");
        }

        if (accountRepository.existsByUser_UserIdAndAccountNameAndIsDeletedFalse(
                currentUserId,
                request.getAccountName()
        )) {
            throw new BusinessException("帳戶名稱已存在");
        }

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到該使用者"));

        BigDecimal initialBalance = request.getInitialBalance();

        if (initialBalance == null) {
            initialBalance = BigDecimal.ZERO;
        }

        Account account = new Account();
        account.setUser(user);
        account.setAccountName(request.getAccountName());
        account.setBalance(initialBalance);
        account.setIsLiability(Boolean.TRUE.equals(request.getIsLiability()));
        account.setIsSavingAccount(Boolean.TRUE.equals(request.getIsSavingAccount()));
        account.setIsDeleted(false);

        Account savedAccount = accountRepository.save(account);

        return AccountMapper.toResponse(savedAccount);
    }

    /**
     * 修改帳戶。
     *
     * 這裡刻意不提供修改 balance，因為 balance 應該由收入、支出、轉帳這些紀錄推動，
     * 不然使用者直接改餘額會造成「帳戶餘額」和「交易紀錄」對不起來。
     */
    @Override
    public AccountResponse updateAccount(
            String currentUserId,
            Integer accountId,
            AccountUpdateRequest request
    ) {
        if (request == null) {
            throw new BusinessException("帳戶修改資料不可為空");
        }

        Account account = accountRepository
                .findByAccountIdAndUser_UserIdAndIsDeletedFalse(accountId, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("帳戶不存在"));

        if (request.getAccountName() != null && !request.getAccountName().isBlank()) {
            boolean sameName = request.getAccountName().equals(account.getAccountName());

            if (!sameName && accountRepository.existsByUser_UserIdAndAccountNameAndIsDeletedFalse(
                    currentUserId,
                    request.getAccountName()
            )) {
                throw new BusinessException("帳戶名稱已存在");
            }

            account.setAccountName(request.getAccountName());
        }

        if (request.getIsLiability() != null) {
            account.setIsLiability(request.getIsLiability());
        }

        if (request.getIsSavingAccount() != null) {
            account.setIsSavingAccount(request.getIsSavingAccount());
        }

        Account savedAccount = accountRepository.save(account);

        return AccountMapper.toResponse(savedAccount);
    }

    /**
     * 停用帳戶。
     *
     * 採用軟刪除：只把 isDeleted 改成 true，不從資料庫真的刪除。
     * 這樣歷史交易、轉帳紀錄仍然可以保留關聯，不會因為帳戶被刪除而壞掉。
     */
    @Override
    public AccountResponse disableAccount(
            String currentUserId,
            Integer accountId
    ) {
        Account account = accountRepository
                .findByAccountIdAndUser_UserIdAndIsDeletedFalse(accountId, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("帳戶不存在"));

        account.setIsDeleted(true);

        Account savedAccount = accountRepository.save(account);

        return AccountMapper.toResponse(savedAccount);
    }

    /**
     * 建立新使用者預設帳戶。
     *
     * 需求文件提到新使用者應有預設帳戶，這裡建立：現金、銀行、信用卡。
     * 如果該使用者已經有帳戶，就直接 return，避免重複建立。
     */
    @Override
    public void createDefaultAccountsForUser(User user) {
        if (user == null || user.getUserId() == null) {
            throw new BusinessException("使用者資料不可為空");
        }

        List<Account> existingAccounts = accountRepository.findByUser_UserId(user.getUserId());

        if (existingAccounts != null && !existingAccounts.isEmpty()) {
            return;
        }

        List<Account> defaultAccounts = new ArrayList<>();

        defaultAccounts.add(createDefaultAccount(
                user,
                "現金",
                BigDecimal.ZERO,
                false,
                false
        ));

        defaultAccounts.add(createDefaultAccount(
                user,
                "銀行",
                BigDecimal.ZERO,
                false,
                false
        ));

        defaultAccounts.add(createDefaultAccount(
                user,
                "信用卡",
                BigDecimal.ZERO,
                true,
                false
        ));

        accountRepository.saveAll(defaultAccounts);
    }

    /** 小工具方法：集中建立 Account Entity，避免上面重複寫很多 setter。 */
    private Account createDefaultAccount(
            User user,
            String accountName,
            BigDecimal balance,
            Boolean isLiability,
            Boolean isSavingAccount
    ) {
        Account account = new Account();
        account.setUser(user);
        account.setAccountName(accountName);
        account.setBalance(balance);
        account.setIsLiability(isLiability);
        account.setIsSavingAccount(isSavingAccount);
        account.setIsDeleted(false);
        return account;
    }
    
    @Override
    public List<AccountResponse> getSavingAccountsOnly(String userId) {
        // 呼叫你在 Repository 補的那行神邏輯
        List<Account> accounts = accountRepository.findByUser_UserIdAndIsSavingAccountTrueAndIsDeletedFalse(userId);
        
        // 轉成 Response 格式
        return accounts.stream()
                .map(AccountMapper::toResponse)
                .collect(Collectors.toList());
    }
}
