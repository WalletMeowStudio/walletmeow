package com.walletpet.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.walletpet.dto.category.CategoryCreateRequest;
import com.walletpet.dto.category.CategoryResponse;
import com.walletpet.dto.category.CategoryUpdateRequest;
import com.walletpet.entity.Category;
import com.walletpet.entity.User;
import com.walletpet.enums.CategoryType;
import com.walletpet.exception.BusinessException;
import com.walletpet.exception.ResourceNotFoundException;
import com.walletpet.mapper.CategoryMapper;
import com.walletpet.repository.CategoryRepository;
import com.walletpet.repository.UserRepository;
import com.walletpet.service.CategoryService;
import com.walletpet.util.IdGenerator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    /**
     * 查詢目前登入者的分類總覽。
     * 
     * 若 type 有傳入，只查該類型分類。
     * 若 type 為 null，查該使用者全部分類。
     * 
     * includeDisabled = true  時，包含停用分類。
     * includeDisabled = false 時，只查啟用分類。
     */
    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> findCategories(
            String currentUserId,
            CategoryType type,
            boolean includeDisabled
    ) {
        List<Category> categories;

        if (type == null) {
            if (includeDisabled) {
                categories = categoryRepository.findByUser_UserId(currentUserId);
            } else {
                categories = categoryRepository.findByUser_UserIdAndIsDisableFalse(currentUserId);
            }
        } else {
            if (includeDisabled) {
                categories = categoryRepository.findByUser_UserIdAndCategoryType(currentUserId, type);
            } else {
                categories = categoryRepository.findByUser_UserIdAndCategoryTypeAndIsDisableFalse(currentUserId, type);
            }
        }

        return categories.stream()
                .map(CategoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 查詢新增 / 編輯交易頁可選用的分類。
     * 
     * 只回傳目前登入者「未停用」的分類。
     * 通常會搭配 type=INCOME 或 type=EXPENSE 使用。
     */
    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> findAvailableCategories(
            String currentUserId,
            CategoryType type
    ) {
        List<Category> categories;

        if (type == null) {
            categories = categoryRepository.findByUser_UserIdAndIsDisableFalse(currentUserId);
        } else {
            categories = categoryRepository.findByUser_UserIdAndCategoryTypeAndIsDisableFalse(currentUserId, type);
        }

        return categories.stream()
                .map(CategoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 查詢單一分類詳細資料。
     * 
     * 只允許查詢目前登入者自己的分類。
     */
    @Override
    @Transactional(readOnly = true)
    public CategoryResponse findById(String currentUserId, String categoryId) {
        Category category = categoryRepository
                .findByCategoryIdAndUser_UserId(categoryId, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("分類不存在"));

        return CategoryMapper.toResponse(category);
    }

    /**
     * 新增使用者自訂分類。
     * 
     * 注意：
     * 1. userId 不從前端傳入，而是從 token 取得 currentUserId。
     * 2. isSystem 固定 false，代表使用者自訂分類。
     * 3. isDisable 固定 false，新增時預設啟用。
     */
    @Override
    public CategoryResponse createCategory(
            String currentUserId,
            CategoryCreateRequest request
    ) {
        if (request == null) {
            throw new BusinessException("分類資料不可為空");
        }

        if (request.getCategoryName() == null || request.getCategoryName().isBlank()) {
            throw new BusinessException("分類名稱不可為空");
        }

        if (request.getCategoryType() == null) {
            throw new BusinessException("分類類型不可為空");
        }

        boolean exists = categoryRepository.existsByUser_UserIdAndCategoryNameAndCategoryType(
                currentUserId,
                request.getCategoryName(),
                request.getCategoryType()
        );

        if (exists) {
            throw new BusinessException("此分類名稱已存在");
        }

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到該使用者"));

        Category category = new Category();
        category.setCategoryId(IdGenerator.generate("CAT"));
        category.setUser(user);
        category.setCategoryName(request.getCategoryName());
        category.setCategoryType(request.getCategoryType());
        category.setIcon(request.getIcon());
        category.setColor(request.getColor());
        category.setIsSystem(false);
        category.setIsDisable(false);

        Category savedCategory = categoryRepository.save(category);

        return CategoryMapper.toResponse(savedCategory);
    }

    /**
     * 修改分類。
     * 
     * 目前建議：
     * 1. 系統預設分類 isSystem = true，不允許修改名稱、icon、color。
     * 2. 但可視你們需求允許停用系統分類。
     * 
     * 如果你們希望系統分類完全不能動，可以把 isDisable 也一起禁止。
     */
    @Override
    public CategoryResponse updateCategory(
            String currentUserId,
            String categoryId,
            CategoryUpdateRequest request
    ) {
        if (request == null) {
            throw new BusinessException("分類修改資料不可為空");
        }

        Category category = categoryRepository
                .findByCategoryIdAndUser_UserId(categoryId, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("分類不存在"));

        boolean isSystemCategory = Boolean.TRUE.equals(category.getIsSystem());

        if (isSystemCategory) {
            // 系統預設分類不允許修改名稱、icon、color
            if (request.getCategoryName() != null
                    || request.getIcon() != null
                    || request.getColor() != null) {
                throw new BusinessException("系統預設分類不可修改名稱、圖示或顏色");
            }

            // 但允許使用者停用自己的預設分類
            if (request.getIsDisable() != null) {
                category.setIsDisable(request.getIsDisable());
            }

            return CategoryMapper.toResponse(categoryRepository.save(category));
        }

        if (request.getCategoryName() != null && !request.getCategoryName().isBlank()) {
            boolean exists = categoryRepository.existsByUser_UserIdAndCategoryNameAndCategoryType(
                    currentUserId,
                    request.getCategoryName(),
                    category.getCategoryType()
            );

            boolean isSameName = request.getCategoryName().equals(category.getCategoryName());

            if (exists && !isSameName) {
                throw new BusinessException("此分類名稱已存在");
            }

            category.setCategoryName(request.getCategoryName());
        }

        if (request.getIcon() != null) {
            category.setIcon(request.getIcon());
        }

        if (request.getColor() != null) {
            category.setColor(request.getColor());
        }

        if (request.getIsDisable() != null) {
            category.setIsDisable(request.getIsDisable());
        }

        Category savedCategory = categoryRepository.save(category);

        return CategoryMapper.toResponse(savedCategory);
    }

    /**
     * 建立新使用者時，同步建立預設分類。
     * 
     * 這個方法會在 UserServiceImpl.registerUser() 裡呼叫。
     * 
     * 注意：
     * 1. 這些分類會直接綁定該使用者。
     * 2. isSystem = true 代表這些分類是系統預設產生。
     * 3. 之後查詢時只查 userId，不需要另外合併全域系統分類。
     */
    @Override
    public void createDefaultCategoriesForUser(User user) {
        if (user == null || user.getUserId() == null) {
            throw new BusinessException("使用者資料不可為空");
        }

        // 避免重複初始化
        List<Category> existingCategories = categoryRepository.findByUser_UserId(user.getUserId());

        if (existingCategories != null && !existingCategories.isEmpty()) {
            return;
        }

        List<Category> defaultCategories = new ArrayList<>();

        // 收入分類
        defaultCategories.add(createDefaultCategory(user, "薪資", CategoryType.INCOME, "salary", "#4CAF50"));
        defaultCategories.add(createDefaultCategory(user, "獎金", CategoryType.INCOME, "bonus", "#8BC34A"));
        defaultCategories.add(createDefaultCategory(user, "投資", CategoryType.INCOME, "investment", "#009688"));
        defaultCategories.add(createDefaultCategory(user, "其他收入", CategoryType.INCOME, "income-other", "#607D8B"));

        // 支出分類
        defaultCategories.add(createDefaultCategory(user, "餐飲", CategoryType.EXPENSE, "food", "#FF9800"));
        defaultCategories.add(createDefaultCategory(user, "交通", CategoryType.EXPENSE, "transport", "#03A9F4"));
        defaultCategories.add(createDefaultCategory(user, "娛樂", CategoryType.EXPENSE, "entertainment", "#9C27B0"));
        defaultCategories.add(createDefaultCategory(user, "購物", CategoryType.EXPENSE, "shopping", "#E91E63"));
        defaultCategories.add(createDefaultCategory(user, "生活用品", CategoryType.EXPENSE, "daily", "#795548"));
        defaultCategories.add(createDefaultCategory(user, "醫療", CategoryType.EXPENSE, "medical", "#F44336"));
        defaultCategories.add(createDefaultCategory(user, "學習", CategoryType.EXPENSE, "education", "#3F51B5"));
        defaultCategories.add(createDefaultCategory(user, "其他支出", CategoryType.EXPENSE, "expense-other", "#9E9E9E"));

        categoryRepository.saveAll(defaultCategories);
    }

    /**
     * 建立預設分類物件。
     */
    private Category createDefaultCategory(User user,String categoryName,CategoryType categoryType,String icon,String color) {
        Category category = new Category();
        category.setCategoryId(IdGenerator.generate("CAT"));
        category.setUser(user);
        category.setCategoryName(categoryName);
        category.setCategoryType(categoryType);
        category.setIcon(icon);
        category.setColor(color);
        category.setIsSystem(true);
        category.setIsDisable(false);
        return category;
    }
}