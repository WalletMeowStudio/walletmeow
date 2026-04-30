package com.walletpet.controller;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.walletpet.dto.common.ApiResponse;
import com.walletpet.dto.transaction.TransactionCreateRequest;
import com.walletpet.dto.transaction.TransactionResponse;
import com.walletpet.dto.transaction.TransactionUpdateRequest;
import com.walletpet.enums.TransactionType;
import com.walletpet.security.CurrentUserUtil;
import com.walletpet.service.TransactionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final CurrentUserUtil currentUserUtil;

    /*
     * 取得新增 / 編輯交易表單需要的帳戶與分類資料
     *
     * GET /walletpet/api/transactions/form-meta
     * GET /walletpet/api/transactions/form-meta?transactionType=EXPENSE
     */
    @GetMapping("/form-meta")
    public ApiResponse<Map<String, Object>> getFormMeta(
            @RequestParam(required = false) TransactionType transactionType
    ) {
        String currentUserId = currentUserUtil.getCurrentUserId();

        Map<String, Object> data = transactionService.getFormMeta(
                currentUserId,
                transactionType
        );

        return ApiResponse.success("查詢成功", data);
    }

    /*
     * 新增交易
     *
     * POST /walletpet/api/transactions
     *
     * 這裡暫時保留 @RequestBody TransactionCreateRequest，
     * 因為你們目前用 Postman 傳 JSON 測試會比較直覺。
     */
    @PostMapping
    public ApiResponse<TransactionResponse> createTransaction(
            @RequestBody TransactionCreateRequest request
    ) {
        String currentUserId = currentUserUtil.getCurrentUserId();

        TransactionResponse data = transactionService.createTransaction(
                currentUserId,
                request
        );

        return ApiResponse.success("新增交易成功", data);
    }

    /*
     * 查詢交易明細
     *
     * GET /walletpet/api/transactions
     * GET /walletpet/api/transactions?startDate=2026-04-01&endDate=2026-04-30&type=EXPENSE
     */
    @GetMapping
    public ApiResponse<Map<String, Object>> searchTransactions(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,

            @RequestParam(required = false)
            Integer accountId,

            @RequestParam(required = false)
            String categoryId,

            @RequestParam(required = false)
            TransactionType type,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size
    ) {
        String currentUserId = currentUserUtil.getCurrentUserId();

        Map<String, Object> data = transactionService.searchTransactions(
                currentUserId,
                startDate,
                endDate,
                accountId,
                categoryId,
                type,
                page,
                size
        );

        return ApiResponse.success("查詢成功", data);
    }

    /*
     * 查詢單筆交易詳細資料
     *
     * GET /walletpet/api/transactions/{id}
     */
    @GetMapping("/{id}")
    public ApiResponse<TransactionResponse> findById(
            @PathVariable String id
    ) {
        String currentUserId = currentUserUtil.getCurrentUserId();

        TransactionResponse data = transactionService.findById(
                currentUserId,
                id
        );

        return ApiResponse.success("查詢成功", data);
    }

    /*
     * 修改交易
     *
     * PUT /walletpet/api/transactions/{id}
     *
     * 這裡暫時保留 @RequestBody TransactionUpdateRequest，
     * 方便用 JSON 測試。
     */
    @PutMapping("/{id}")
    public ApiResponse<TransactionResponse> updateTransaction(
            @PathVariable String id,
            @RequestBody TransactionUpdateRequest request
    ) {
        String currentUserId = currentUserUtil.getCurrentUserId();

        TransactionResponse data = transactionService.updateTransaction(
                currentUserId,
                id,
                request
        );

        return ApiResponse.success("交易修改成功", data);
    }

    /*
     * 刪除交易
     *
     * DELETE /walletpet/api/transactions/{id}
     */
    @DeleteMapping("/{id}")
    public ApiResponse<TransactionResponse> deleteTransaction(
            @PathVariable String id
    ) {
        String currentUserId = currentUserUtil.getCurrentUserId();

        TransactionResponse data = transactionService.deleteTransaction(
                currentUserId,
                id
        );

        return ApiResponse.success("交易刪除成功", data);
    }

    /*
     * 查詢交易摘要
     *
     * GET /walletpet/api/transactions/summary
     * GET /walletpet/api/transactions/summary?startDate=2026-04-01&endDate=2026-04-30
     */
    @GetMapping("/summary")
    public ApiResponse<Map<String, Object>> getSummary(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,

            @RequestParam(required = false)
            Integer accountId,

            @RequestParam(required = false)
            String categoryId
    ) {
        String currentUserId = currentUserUtil.getCurrentUserId();

        Map<String, Object> data = transactionService.getSummary(
                currentUserId,
                startDate,
                endDate,
                accountId,
                categoryId
        );

        return ApiResponse.success("查詢成功", data);
    }
}