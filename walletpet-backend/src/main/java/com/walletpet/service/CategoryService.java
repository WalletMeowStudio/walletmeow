package com.walletpet.service;

import java.util.List;

import com.walletpet.dto.category.CategoryCreateRequest;
import com.walletpet.dto.category.CategoryResponse;
import com.walletpet.dto.category.CategoryUpdateRequest;
import com.walletpet.entity.User;
import com.walletpet.enums.CategoryType;

public interface CategoryService {

    List<CategoryResponse> findCategories(String currentUserId, CategoryType type, boolean includeDisabled);

    List<CategoryResponse> findAvailableCategories(String currentUserId, CategoryType type);

    CategoryResponse findById(String currentUserId, String categoryId);

    CategoryResponse createCategory(String currentUserId, CategoryCreateRequest request);

    CategoryResponse updateCategory(String currentUserId, String categoryId, CategoryUpdateRequest request);
    
    void createDefaultCategoriesForUser(User user);
}