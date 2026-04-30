package com.walletpet.dto.category;

import com.walletpet.enums.CategoryType;
import lombok.Data;

@Data
public class CategoryCreateRequest {

    private String categoryName;

    private CategoryType categoryType;

    private String icon;

    private String color;
}