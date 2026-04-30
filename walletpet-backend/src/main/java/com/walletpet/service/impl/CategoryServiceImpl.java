package com.walletpet.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.walletpet.dto.category.CategoryResponse;
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

    /*
     * 查詢目前登入者的分類總覽。
     *
     * type == null：
     * 查該使用者全部分類。
     *
     * includeDisabled == false：
     * 只查啟用分類。
     *
     * includeDisabled == true：
     * 包含停用分類。
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

    /*
     * 查詢新增 / 編輯交易頁可選用分類。
     * 只回傳目前登入者未停用分類。
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
            categories = categoryRepository.findByUser_UserIdAndCategoryTypeAndIsDisableFalse(
                    currentUserId,
                    type
            );
        }

        return categories.stream()
                .map(CategoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    /*
     * 查詢單一分類。
     * 只能查目前登入者自己的分類。
     */
    @Override
    @Transactional(readOnly = true)
    public CategoryResponse findById(
            String currentUserId,
            String categoryId
    ) {
        Category category = categoryRepository
                .findByCategoryIdAndUser_UserId(categoryId, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("分類不存在"));

        return CategoryMapper.toResponse(category);
    }

    /*
     * 新增使用者自訂分類。
     *
     * DTO 最小化後，不再使用 CategoryCreateRequest。
     * Controller 直接用 @RequestParam 傳入表單欄位。
     *
     * 注意：
     * 1. userId 不從前端傳入。
     * 2. userId 一律由 token 取得 currentUserId。
     * 3. isSystem 固定 false。
     * 4. isDisable 固定 false。
     */
    @Override
    public CategoryResponse createCategory(
            String currentUserId,
            String categoryName,
            CategoryType categoryType,
            String icon,
            String color
    ) {
        validateCategoryName(categoryName);

        if (categoryType == null) {
            throw new BusinessException("分類類型不可為空");
        }

        String normalizedCategoryName = categoryName.trim();

        boolean exists = categoryRepository.existsByUser_UserIdAndCategoryNameAndCategoryType(
                currentUserId,
                normalizedCategoryName,
                categoryType
        );

        if (exists) {
            throw new BusinessException("此分類名稱已存在");
        }

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到該使用者"));

        Category category = new Category();
        category.setCategoryId(IdGenerator.generate("CAT"));
        category.setUser(user);
        category.setCategoryName(normalizedCategoryName);
        category.setCategoryType(categoryType);
        category.setIcon(resolveIcon(icon));
        category.setColor(normalizeNullableText(color));
        category.setIsSystem(false);
        category.setIsDisable(false);

        Category savedCategory = categoryRepository.save(category);

        return CategoryMapper.toResponse(savedCategory);
    }

    /*
     * 修改分類。
     *
     * DTO 最小化後，不再使用 CategoryUpdateRequest。
     *
     * 規則：
     * 1. 系統預設分類 isSystem = true：
     *    不允許修改 categoryName / icon / color。
     *    但允許停用或啟用。
     *
     * 2. 使用者自訂分類 isSystem = false：
     *    可修改 categoryName / icon / color / isDisable。
     *
     * 3. 不允許改 categoryType：
     *    因為分類一旦被交易使用，改類型容易造成交易統計錯亂。
     */
    @Override
    public CategoryResponse updateCategory(
            String currentUserId,
            String categoryId,
            String categoryName,
            String icon,
            String color,
            Boolean isDisable
    ) {
        Category category = categoryRepository
                .findByCategoryIdAndUser_UserId(categoryId, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("分類不存在"));

        boolean isSystemCategory = Boolean.TRUE.equals(category.getIsSystem());

        if (isSystemCategory) {
            boolean wantsToChangeBasicInfo =
                    hasText(categoryName) || hasText(icon) || hasText(color);

            if (wantsToChangeBasicInfo) {
                throw new BusinessException("系統預設分類不可修改名稱、圖示或顏色");
            }

            if (isDisable != null) {
                category.setIsDisable(isDisable);
            }

            Category savedCategory = categoryRepository.save(category);
            return CategoryMapper.toResponse(savedCategory);
        }

        if (categoryName != null) {
            validateCategoryName(categoryName);

            String normalizedCategoryName = categoryName.trim();

            boolean isSameName = normalizedCategoryName.equals(category.getCategoryName());

            boolean exists = categoryRepository.existsByUser_UserIdAndCategoryNameAndCategoryType(
                    currentUserId,
                    normalizedCategoryName,
                    category.getCategoryType()
            );

            if (exists && !isSameName) {
                throw new BusinessException("此分類名稱已存在");
            }

            category.setCategoryName(normalizedCategoryName);
        }

        if (icon != null) {
            category.setIcon(resolveIcon(icon));
        }

        if (color != null) {
            category.setColor(normalizeNullableText(color));
        }

        if (isDisable != null) {
            category.setIsDisable(isDisable);
        }

        Category savedCategory = categoryRepository.save(category);

        return CategoryMapper.toResponse(savedCategory);
    }

    /*
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

        List<Category> existingCategories = categoryRepository.findByUser_UserId(user.getUserId());

        if (existingCategories != null && !existingCategories.isEmpty()) {
            return;
        }

        List<Category> defaultCategories = new ArrayList<>();

        // 收入分類
        defaultCategories.add(createDefaultCategory(user, "薪資", CategoryType.INCOME, "💰", "#4CAF50"));
        defaultCategories.add(createDefaultCategory(user, "獎金", CategoryType.INCOME, "🎁", "#8BC34A"));
        defaultCategories.add(createDefaultCategory(user, "利息", CategoryType.INCOME, "🏦", "#009688"));
        defaultCategories.add(createDefaultCategory(user, "投資", CategoryType.INCOME, "📈", "#6AA35D"));
        defaultCategories.add(createDefaultCategory(user, "其他收入", CategoryType.INCOME, "💵", "#607D8B"));

        // 支出分類
        defaultCategories.add(createDefaultCategory(user, "餐飲", CategoryType.EXPENSE, "🍜", "#FF9800"));
        defaultCategories.add(createDefaultCategory(user, "衣飾", CategoryType.EXPENSE, "👕", "#E91E63"));
        defaultCategories.add(createDefaultCategory(user, "家居", CategoryType.EXPENSE, "🏠", "#795548"));
        defaultCategories.add(createDefaultCategory(user, "交通", CategoryType.EXPENSE, "🚇", "#03A9F4"));
        defaultCategories.add(createDefaultCategory(user, "學習", CategoryType.EXPENSE, "📚", "#3F51B5"));
        defaultCategories.add(createDefaultCategory(user, "娛樂", CategoryType.EXPENSE, "🎬", "#9C27B0"));
        defaultCategories.add(createDefaultCategory(user, "醫療", CategoryType.EXPENSE, "🏥", "#F44336"));
        defaultCategories.add(createDefaultCategory(user, "其他支出", CategoryType.EXPENSE, "🛒", "#9E9E9E"));

        categoryRepository.saveAll(defaultCategories);
    }

    private Category createDefaultCategory(User user,String categoryName,
    		CategoryType categoryType,String icon,String color) {
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

    private void validateCategoryName(String categoryName) {
        if (!hasText(categoryName)) {
            throw new BusinessException("分類名稱不可為空");
        }

        if (categoryName.trim().length() > 50) {
            throw new BusinessException("分類名稱不可超過 50 個字");
        }
    }

    private String resolveIcon(String icon) {
        String normalizedIcon = normalizeNullableText(icon);

        if (normalizedIcon == null) {
            return "default";
        }

        return normalizedIcon;
    }

    private String normalizeNullableText(String value) {
        if (value == null) {
            return null;
        }

        String trimmedValue = value.trim();

        if (trimmedValue.isEmpty()) {
            return null;
        }

        return trimmedValue;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}