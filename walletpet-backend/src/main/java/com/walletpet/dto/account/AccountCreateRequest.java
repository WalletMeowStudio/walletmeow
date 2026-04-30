package com.walletpet.dto.account;

import java.math.BigDecimal;

import lombok.Data;

/**
 * 新增帳戶 API 的前端傳入資料。
 *
 * POST /api/accounts
 * 前端會把表單輸入的帳戶名稱、初始餘額、是否負債、是否存款帳戶放在這個 DTO。
 */
@Data
public class AccountCreateRequest {

    /** 帳戶名稱，例如：現金、銀行、信用卡。 */
    private String accountName;

    /** 初始餘額；如果前端沒傳，Service 會預設為 0。 */
    private BigDecimal initialBalance;

    /** 是否為負債帳戶，會影響帳戶總覽 summary 的總負債計算。 */
    private Boolean isLiability;

    /** 是否為存款帳戶，給存錢目標或特殊篩選使用。 */
    private Boolean isSavingAccount;
}
