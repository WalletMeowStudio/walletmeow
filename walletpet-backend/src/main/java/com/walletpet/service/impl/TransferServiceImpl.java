package com.walletpet.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.walletpet.dto.transfer.TransferAccountBalanceResponse;
import com.walletpet.dto.transfer.TransferCreateRequest;
import com.walletpet.dto.transfer.TransferResponse;
import com.walletpet.entity.Account;
import com.walletpet.entity.AccountTransaction;
import com.walletpet.entity.User;
import com.walletpet.exception.BusinessException;
import com.walletpet.exception.ResourceNotFoundException;
import com.walletpet.mapper.TransferMapper;
import com.walletpet.repository.AccountRepository;
import com.walletpet.repository.AccountTransactionRepository;
import com.walletpet.repository.UserRepository;
import com.walletpet.service.TransferService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TransferServiceImpl implements TransferService {

    private final AccountTransactionRepository accountTransactionRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Override
    public TransferResponse createTransfer(String currentUserId, TransferCreateRequest request) {
        validateCreateRequest(request);

        if (request.getFromAccountId().equals(request.getToAccountId())) {
            throw new BusinessException("轉出帳戶與轉入帳戶不可相同");
        }

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到該使用者"));

        Account fromAccount = accountRepository
                .findByAccountIdAndUser_UserIdAndIsDeletedFalse(request.getFromAccountId(), currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("轉出帳戶不存在或已停用"));

        Account toAccount = accountRepository
                .findByAccountIdAndUser_UserIdAndIsDeletedFalse(request.getToAccountId(), currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("轉入帳戶不存在或已停用"));

        BigDecimal amount = request.getTransactionAmount();

        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new BusinessException("轉出帳戶餘額不足");
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));

        AccountTransaction transfer = new AccountTransaction();
        transfer.setUser(user);
        transfer.setFromAccount(fromAccount);
        transfer.setToAccount(toAccount);
        transfer.setTransactionAmount(amount);
        transfer.setTransactionDate(request.getTransactionDate());
        transfer.setNote(request.getNote());

        AccountTransaction savedTransfer = accountTransactionRepository.save(transfer);

        return TransferMapper.toResponse(savedTransfer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransferResponse> findTransfers(String currentUserId) {
        return accountTransactionRepository.findByUser_UserIdOrderByTransactionDateDescCreatedAtDesc(currentUserId)
                .stream()
                .map(TransferMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TransferResponse findById(String currentUserId, Integer accountTransId) {
        AccountTransaction transfer = accountTransactionRepository
                .findByAccountTransIdAndUser_UserId(accountTransId, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("轉帳紀錄不存在"));

        return TransferMapper.toResponse(transfer);
    }

    @Override
    public TransferAccountBalanceResponse deleteTransfer(String currentUserId, Integer accountTransId) {
        AccountTransaction transfer = accountTransactionRepository
                .findByAccountTransIdAndUser_UserId(accountTransId, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("轉帳紀錄不存在"));

        Account fromAccount = transfer.getFromAccount();
        Account toAccount = transfer.getToAccount();
        BigDecimal amount = transfer.getTransactionAmount();

        // 刪除轉帳時要回沖：原本扣掉的轉出帳戶補回來，原本增加的轉入帳戶扣回來。
        fromAccount.setBalance(fromAccount.getBalance().add(amount));
        toAccount.setBalance(toAccount.getBalance().subtract(amount));

        accountTransactionRepository.delete(transfer);

        return new TransferAccountBalanceResponse(
                fromAccount.getAccountId(),
                fromAccount.getIsDeleted(),
                fromAccount.getBalance()
        );
    }

    private void validateCreateRequest(TransferCreateRequest request) {
        if (request == null) {
            throw new BusinessException("轉帳資料不可為空");
        }

        if (request.getFromAccountId() == null) {
            throw new BusinessException("轉出帳戶不可為空");
        }

        if (request.getToAccountId() == null) {
            throw new BusinessException("轉入帳戶不可為空");
        }

        if (request.getTransactionAmount() == null || request.getTransactionAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("轉帳金額必須大於 0");
        }

        if (request.getTransactionDate() == null) {
            request.setTransactionDate(LocalDate.now());
        }
    }
}
