package com.walletpet.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Account Entity 對應資料庫 accounts 表格。
 *
 * Entity 的責任是「資料表對應物件」：
 * - accountId 對應 accounts.account_id
 * - accountName 對應 accounts.account_name
 * - balance 對應 accounts.balance
 * - user 對應此帳戶屬於哪一個使用者
 *
 * 注意：這個類別不是拿來直接回傳前端的，前端回傳格式請看 AccountResponse DTO。
 */
@Entity
@Table(name = "accounts")
@EqualsAndHashCode(exclude = {"user", "outgoingTransfers", "incomingTransfers"})
@EntityListeners(AuditingEntityListener.class)
@Data
public class Account {

    /** 帳戶主鍵，對應 accounts.account_id。 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Integer accountId;

    /** 帳戶名稱，例如：現金、銀行、信用卡。 */
    @Column(name = "account_name", nullable = false, length = 50)
    private String accountName;

    /** 帳戶目前餘額。收入、支出、轉帳都會影響這個欄位。 */
    @Column(name = "balance", nullable = false, precision = 12, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    /** 是否為負債帳戶，例如信用卡、貸款。帳戶總覽 summary 會用它判斷總資產或總負債。 */
    @Column(name = "is_liability", nullable = false)
    private Boolean isLiability = false;

    /** 是否停用。採軟刪除設計，不直接刪除資料，避免歷史交易關聯中斷。 */
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    /** 是否為存款帳戶，供存錢目標或特殊帳戶篩選使用。 */
    @Column(name = "is_saving_account", nullable = false)
    private Boolean isSavingAccount = false;

    /** 建立時間，由資料庫或 JPA 自動產生。 */
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    /**
     * 多對一：多個 Account 屬於同一個 User。
     * accounts.user_id 是外鍵，對應 users.user_id。
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /*
     * 如果之後 Transaction Entity 有 account 欄位，可以打開這段一對多關聯：
     * 一個帳戶可以有多筆收入／支出交易。
     */
    /*
    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
    private Set<Transaction> transactions;
    */

    /**
     * 一對多：一個帳戶可以有多筆「轉出」紀錄。
     *
     * mappedBy = "fromAccount" 的意思：
     * 關聯的主控欄位在 AccountTransaction.fromAccount，
     * 也就是 account_transactions.from_account_id。
     */
    @JsonIgnore
    @OneToMany(mappedBy = "fromAccount", fetch = FetchType.LAZY)
    private Set<AccountTransaction> outgoingTransfers;

    /**
     * 一對多：一個帳戶可以有多筆「轉入」紀錄。
     *
     * mappedBy = "toAccount" 的意思：
     * 關聯的主控欄位在 AccountTransaction.toAccount，
     * 也就是 account_transactions.to_account_id。
     */
    @JsonIgnore
    @OneToMany(mappedBy = "toAccount", fetch = FetchType.LAZY)
    private Set<AccountTransaction> incomingTransfers;
}
