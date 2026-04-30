package com.walletpet.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.walletpet.entity.Category;
import com.walletpet.enums.CategoryType;

public interface CategoryRepository extends JpaRepository<Category, String> {

    List<Category> findByUser_UserIdAndCategoryType(String userId,CategoryType categoryType);

    List<Category> findByUser_UserIdAndCategoryTypeAndIsDisableFalse(String userId,CategoryType categoryType);

    List<Category> findByUser_UserIdAndIsDisableFalse(String userId);

    List<Category> findByUser_UserId(String userId);

    Optional<Category> findByCategoryIdAndUser_UserId(String categoryId,String userId);

    boolean existsByUser_UserIdAndCategoryNameAndCategoryType(String userId,String categoryName,CategoryType categoryType);
}