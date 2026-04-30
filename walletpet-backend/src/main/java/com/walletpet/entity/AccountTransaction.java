package com.walletpet.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AccountTransaction Entity 對應資料庫 account_transactions 表格。
 *
 * 這張表專門記錄「帳戶與帳戶之間的轉帳」，
 * 例如：現金轉到銀行、銀行轉到存款帳戶。
 *
 * 為什麼不放在 transactions？
 * 因為 transactions 主要放收入／支出；轉帳不是真的收入或支出，
 * 只是錢從一個帳戶移到另一個帳戶，分表會讓報表統計比較清楚。
 */
@Entity
@Table(name = "account_transactions")
@EqualsAndHashCode(exclude = {"user", "fromAccount", "toAccount"})
@Data
public class AccountTransaction {

    /** 轉帳紀錄主鍵，對應 account_transactions.account_trans_id。 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_trans_id")
    private Integer accountTransId;

    /**
     * 多對一：多筆轉帳紀錄屬於同一個使用者。
     * 後端用 currentUserId 限制只能查自己的轉帳資料。
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 多對一：多筆轉帳紀錄可以從同一個帳戶轉出。
     * 對應 account_transactions.from_account_id。
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_account_id", nullable = false)
    private Account fromAccount;

    /**
     * 多對一：多筆轉帳紀錄可以轉入同一個帳戶。
     * 對應 account_transactions.to_account_id。
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_account_id", nullable = false)
    private Account toAccount;

    /** 轉帳金額，必須大於 0。 */
    @Column(name = "transaction_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal transactionAmount;

    /** 備註，例如：轉到旅遊基金。 */
    @Column(name = "note", length = 255)
    private String note;

    /** 使用者輸入的轉帳日期。 */
    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    /** 這筆轉帳紀錄建立的時間。 */
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
