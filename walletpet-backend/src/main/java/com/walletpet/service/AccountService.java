package com.walletpet.service;

import java.util.List;

import com.walletpet.dto.account.AccountCreateRequest;
import com.walletpet.dto.account.AccountResponse;
import com.walletpet.dto.account.AccountSummaryResponse;
import com.walletpet.dto.account.AccountUpdateRequest;
import com.walletpet.entity.User;

/**
 * AccountService 是帳戶模組的「商業邏輯介面」。
 *
 * Controller 只負責接收 API 請求；真正的帳戶規則，例如：
 * - 帳戶名稱不可重複
 * - 帳戶只能查詢自己的資料
 * - 停用帳戶採用 isDeleted 軟刪除
 * - 帳戶總覽要計算總資產、總負債、淨資產
 * 都放在 Service 實作類別中處理。
 */
public interface AccountService {

    /**
     * 查詢目前登入者的帳戶列表。
     * includeDeleted=false 時，只回傳未停用帳戶，給前端帳戶總覽使用。
     */
    List<AccountResponse> findAccounts(String currentUserId, boolean includeDeleted);

    /**
     * 計算帳戶總覽頁上方的摘要卡片：總資產、總負債、淨資產。
     */
    AccountSummaryResponse getAccountSummary(String currentUserId);

    /** 查詢單一帳戶詳細資料，並限制只能查自己的帳戶。 */
    AccountResponse findById(String currentUserId, Integer accountId);

    /** 新增帳戶，初始餘額會直接寫入 accounts.balance。 */
    AccountResponse createAccount(String currentUserId, AccountCreateRequest request);

    /** 修改帳戶基本資料，不直接修改 balance，避免破壞交易紀錄一致性。 */
    AccountResponse updateAccount(String currentUserId, Integer accountId, AccountUpdateRequest request);

    /** 停用帳戶：採軟刪除 isDeleted=true，不直接刪除資料。 */
    AccountResponse disableAccount(String currentUserId, Integer accountId);

    /** 新使用者註冊後建立預設帳戶，例如現金、銀行、信用卡。 */
    void createDefaultAccountsForUser(User user);
    
    public List<AccountResponse> getSavingAccountsOnly(String userId);
}
