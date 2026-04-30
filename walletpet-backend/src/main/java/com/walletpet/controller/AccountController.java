package com.walletpet.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.walletpet.dto.account.AccountCreateRequest;
import com.walletpet.dto.account.AccountResponse;
import com.walletpet.dto.account.AccountSummaryResponse;
import com.walletpet.dto.account.AccountUpdateRequest;
import com.walletpet.dto.common.ApiResponse;
import com.walletpet.security.CurrentUserUtil;
import com.walletpet.service.AccountService;

import lombok.RequiredArgsConstructor;

/**
 * AccountController 是帳戶模組的 API 入口。
 *
 * Controller 的責任：
 * 1. 接收前端 HTTP 請求。
 * 2. 從 token 取得目前登入者 currentUserId。
 * 3. 呼叫 AccountService 處理商業邏輯。
 * 4. 統一用 ApiResponse 回傳 JSON 給前端。
 */
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final CurrentUserUtil currentUserUtil;

    // 查詢目前登入者的帳戶列表
    // GET http://localhost:8080/walletpet/api/accounts?includeDeleted=false
    @GetMapping
    public ApiResponse<List<AccountResponse>> findAccounts(
            @RequestParam(defaultValue = "false") boolean includeDeleted
    ) {
        String currentUserId = currentUserUtil.getCurrentUserId();

        List<AccountResponse> data = accountService.findAccounts(
                currentUserId,
                includeDeleted
        );

        return ApiResponse.success("查詢成功", data);
    }

    // 查詢帳戶總覽摘要：總資產、總負債、淨資產
    // GET http://localhost:8080/walletpet/api/accounts/summary
    // 這個 API 給 accounts.html 上方摘要卡片使用，不需要前端自己計算。
    @GetMapping("/summary")
    public ApiResponse<AccountSummaryResponse> getAccountSummary() {
        String currentUserId = currentUserUtil.getCurrentUserId();

        AccountSummaryResponse data = accountService.getAccountSummary(currentUserId);

        return ApiResponse.success("查詢帳戶總覽成功", data);
    }

    // 查詢單一帳戶
    // GET http://localhost:8080/walletpet/api/accounts/{id}
    @GetMapping("/{id}")
    public ApiResponse<AccountResponse> findById(
            @PathVariable Integer id
    ) {
        String currentUserId = currentUserUtil.getCurrentUserId();

        AccountResponse data = accountService.findById(
                currentUserId,
                id
        );

        return ApiResponse.success("查詢成功", data);
    }

    // 新增帳戶
    // POST http://localhost:8080/walletpet/api/accounts
    @PostMapping
    public ApiResponse<AccountResponse> createAccount(
            @RequestBody AccountCreateRequest request
    ) {
        String currentUserId = currentUserUtil.getCurrentUserId();

        AccountResponse data = accountService.createAccount(
                currentUserId,
                request
        );

        return ApiResponse.success("新增帳戶成功", data);
    }

    // 修改帳戶
    // PUT http://localhost:8080/walletpet/api/accounts/{id}
    @PutMapping("/{id}")
    public ApiResponse<AccountResponse> updateAccount(
            @PathVariable Integer id,
            @RequestBody AccountUpdateRequest request
    ) {
        String currentUserId = currentUserUtil.getCurrentUserId();

        AccountResponse data = accountService.updateAccount(
                currentUserId,
                id,
                request
        );

        return ApiResponse.success("修改帳戶成功", data);
    }

    // 停用帳戶，不做實體刪除
    // DELETE http://localhost:8080/walletpet/api/accounts/{id}
    @DeleteMapping("/{id}")
    public ApiResponse<AccountResponse> disableAccount(
            @PathVariable Integer id
    ) {
        String currentUserId = currentUserUtil.getCurrentUserId();

        AccountResponse data = accountService.disableAccount(
                currentUserId,
                id
        );

        return ApiResponse.success("帳戶已停用", data);
    }
    
    @GetMapping("/saving-only")
    public ApiResponse<List<AccountResponse>> getSavingAccountsOnly() {
        String userId = currentUserUtil.getCurrentUserId();
        
        // 正確地透過 Service 呼叫，不直接碰 Repository
        List<AccountResponse> responseList = accountService.getSavingAccountsOnly(userId);
                
        return ApiResponse.success("成功取得可用的存款帳戶清單", responseList);
    }
}
