package com.walletpet.dto.category;

import lombok.Data;

@Data
public class CategoryUpdateRequest {

    private String categoryName;

    private String icon;

    private String color;

    private Boolean isDisable;
}