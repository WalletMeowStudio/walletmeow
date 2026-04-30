package com.walletpet.dto.transaction;

import java.math.BigDecimal;
import java.util.List;

import com.walletpet.enums.CategoryType;

import lombok.Data;

@Data
public class TransactionFormMetaResponse {

    private List<AccountOption> accounts;

    private List<CategoryOption> categories;

    @Data
    public static class AccountOption {

        private Integer accountId;

        private String accountName;

        private BigDecimal balance;

        private Boolean isSavingAccount;
    }

    @Data
    public static class CategoryOption {

        private String categoryId;

        private String categoryName;

        private CategoryType categoryType;

        private String icon;

        private String color;
    }
}