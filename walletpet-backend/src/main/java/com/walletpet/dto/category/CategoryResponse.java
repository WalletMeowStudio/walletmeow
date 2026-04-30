package com.walletpet.dto.category;

import java.time.LocalDateTime;

import com.walletpet.enums.CategoryType;

import lombok.Data;

@Data
public class CategoryResponse {

    private String categoryId;

    private String userId;

    private String categoryName;

    private CategoryType categoryType;

    private String icon;

    private String color;

    private Boolean isSystem;

    private Boolean isDisable;

    private LocalDateTime createdAt;
}