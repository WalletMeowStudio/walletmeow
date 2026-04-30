package com.walletpet.service;

import java.util.List;

import com.walletpet.dto.transfer.TransferAccountBalanceResponse;
import com.walletpet.dto.transfer.TransferCreateRequest;
import com.walletpet.dto.transfer.TransferResponse;

public interface TransferService {

    TransferResponse createTransfer(String currentUserId, TransferCreateRequest request);

    List<TransferResponse> findTransfers(String currentUserId);

    TransferResponse findById(String currentUserId, Integer accountTransId);

    TransferAccountBalanceResponse deleteTransfer(String currentUserId, Integer accountTransId);
}
