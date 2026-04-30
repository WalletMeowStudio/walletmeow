package com.walletpet.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.walletpet.dto.common.ApiResponse;
import com.walletpet.dto.transfer.TransferAccountBalanceResponse;
import com.walletpet.dto.transfer.TransferCreateRequest;
import com.walletpet.dto.transfer.TransferResponse;
import com.walletpet.security.CurrentUserUtil;
import com.walletpet.service.TransferService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;
    private final CurrentUserUtil currentUserUtil;

    // 新增轉帳
    // POST http://localhost:8080/walletpet/api/transfers
    @PostMapping
    public ApiResponse<TransferResponse> createTransfer(
            @RequestBody TransferCreateRequest request
    ) {
        String currentUserId = currentUserUtil.getCurrentUserId();

        TransferResponse data = transferService.createTransfer(currentUserId, request);

        return ApiResponse.success("轉帳成功", data);
    }

    // 查詢目前登入者的所有轉帳紀錄，方便帳戶總覽或轉帳頁測試
    // GET http://localhost:8080/walletpet/api/transfers
    @GetMapping
    public ApiResponse<List<TransferResponse>> findTransfers() {
        String currentUserId = currentUserUtil.getCurrentUserId();

        List<TransferResponse> data = transferService.findTransfers(currentUserId);

        return ApiResponse.success("查詢成功", data);
    }

    // 查詢單筆轉帳紀錄
    // GET http://localhost:8080/walletpet/api/transfers/{id}
    @GetMapping("/{id}")
    public ApiResponse<TransferResponse> findById(
            @PathVariable Integer id
    ) {
        String currentUserId = currentUserUtil.getCurrentUserId();

        TransferResponse data = transferService.findById(currentUserId, id);

        return ApiResponse.success("查詢成功", data);
    }

    // 刪除轉帳紀錄並回沖帳戶餘額
    // DELETE http://localhost:8080/walletpet/api/transfers/{id}
    @DeleteMapping("/{id}")
    public ApiResponse<TransferAccountBalanceResponse> deleteTransfer(
            @PathVariable Integer id
    ) {
        String currentUserId = currentUserUtil.getCurrentUserId();

        TransferAccountBalanceResponse data = transferService.deleteTransfer(currentUserId, id);

        return ApiResponse.success("轉帳紀錄已刪除，帳戶餘額已回沖", data);
    }
}
