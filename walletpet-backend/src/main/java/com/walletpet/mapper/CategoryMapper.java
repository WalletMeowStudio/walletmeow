package com.walletpet.mapper;

import com.walletpet.dto.category.CategoryResponse;
import com.walletpet.entity.Category;

public class CategoryMapper {

    private CategoryMapper() {

    }

    public static CategoryResponse toResponse(Category category) {
        if (category == null) {
            return null;
        }

        CategoryResponse response = new CategoryResponse();

        response.setCategoryId(category.getCategoryId());
        response.setCategoryName(category.getCategoryName());
        response.setCategoryType(category.getCategoryType());
        response.setIcon(category.getIcon());
        response.setColor(category.getColor());
        response.setIsDisable(category.getIsDisable());
        response.setIsSystem(category.getIsSystem());
        response.setCreatedAt(category.getCreatedAt());

        return response;
    }
}