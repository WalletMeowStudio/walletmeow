package com.walletpet.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.walletpet.entity.Transaction;
import com.walletpet.enums.TransactionType;

public interface TransactionRepository extends JpaRepository<Transaction, String> {

    Optional<Transaction> findByTransactionIdAndUser_UserId(
    		String transactionId,String userId);

    List<Transaction> findByUser_UserIdAndTransactionDateOrderByCreatedAtDesc(
            String userId,LocalDate transactionDate);

    List<Transaction> findByUser_UserIdAndTransactionDateBetweenOrderByTransactionDateDescCreatedAtDesc(
            String userId,LocalDate startDate,LocalDate endDate);

    int countByUser_UserIdAndTransactionDateAndTransactionTypeIn(
            String userId,LocalDate transactionDate,List<TransactionType> transactionTypes);

    @Query("""
        SELECT t
        FROM Transaction t
        WHERE t.user.userId = :userId
          AND (:startDate IS NULL OR t.transactionDate >= :startDate)
          AND (:endDate IS NULL OR t.transactionDate <= :endDate)
          AND (:accountId IS NULL OR t.account.accountId = :accountId)
          AND (:categoryId IS NULL OR t.category.categoryId = :categoryId)
          AND (:type IS NULL OR t.transactionType = :type)
    """)
    Page<Transaction> searchTransactions(
            @Param("userId") String userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("accountId") Integer accountId,
            @Param("categoryId") String categoryId,
            @Param("type") TransactionType type,
            Pageable pageable
    );

    @Query("""
        SELECT t
        FROM Transaction t
        WHERE t.user.userId = :userId
          AND (:startDate IS NULL OR t.transactionDate >= :startDate)
          AND (:endDate IS NULL OR t.transactionDate <= :endDate)
          AND (:accountId IS NULL OR t.account.accountId = :accountId)
          AND (:categoryId IS NULL OR t.category.categoryId = :categoryId)
          AND (:type IS NULL OR t.transactionType = :type)
    """)
    List<Transaction> searchTransactionsForSummary(
            @Param("userId") String userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("accountId") Integer accountId,
            @Param("categoryId") String categoryId,
            @Param("type") TransactionType type
    );
}