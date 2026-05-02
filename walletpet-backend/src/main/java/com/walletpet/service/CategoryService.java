package com.walletpet.service;

import java.util.List;

import com.walletpet.dto.category.CategoryResponse;
import com.walletpet.entity.User;
import com.walletpet.enums.CategoryType;

public interface CategoryService {

    List<CategoryResponse> findCategories(String currentUserId,CategoryType type,boolean includeDisabled);

    List<CategoryResponse> findAvailableCategories(String currentUserId,CategoryType type);

    CategoryResponse findById(String currentUserId,String categoryId);

    CategoryResponse createCategory(String currentUserId,String categoryName,
    		CategoryType categoryType,String icon,String color);

    CategoryResponse updateCategory(String currentUserId,String categoryId,String categoryName,
            String icon,String color,Boolean isDisable);

    void createDefaultCategoriesForUser(User user);
}