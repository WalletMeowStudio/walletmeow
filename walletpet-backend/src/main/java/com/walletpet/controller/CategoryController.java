package com.walletpet.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.walletpet.dto.category.CategoryResponse;
import com.walletpet.dto.common.ApiResponse;
import com.walletpet.enums.CategoryType;
import com.walletpet.security.CurrentUserUtil;
import com.walletpet.service.CategoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final CurrentUserUtil currentUserUtil;

    /*
     * 查詢目前登入者的分類總覽
     *
     * GET /walletpet/api/categories
     * GET /walletpet/api/categories?type=EXPENSE
     * GET /walletpet/api/categories?type=EXPENSE&includeDisabled=true
     */
    @GetMapping
    public ApiResponse<List<CategoryResponse>> findCategories(
            @RequestParam(required = false) CategoryType type,
            @RequestParam(defaultValue = "false") boolean includeDisabled) {
        String currentUserId = currentUserUtil.getCurrentUserId();

        List<CategoryResponse> data = categoryService.findCategories(
                currentUserId,type,includeDisabled);

        return ApiResponse.success("查詢成功", data);
    }

    /*
     * 查詢新增交易頁可選用的啟用分類清單
     *
     * GET /walletpet/api/categories/available
     * GET /walletpet/api/categories/available?type=EXPENSE
     */
    @GetMapping("/available")
    public ApiResponse<List<CategoryResponse>> findAvailableCategories(
            @RequestParam(required = false) CategoryType type) {
        String currentUserId = currentUserUtil.getCurrentUserId();

        List<CategoryResponse> data = categoryService.findAvailableCategories(
                currentUserId,type);

        return ApiResponse.success("查詢成功", data);
    }

    /*
     * 查詢單一分類詳細資料
     *
     * GET /walletpet/api/categories/{id}
     */
    @GetMapping("/{id}")
    public ApiResponse<CategoryResponse> findById(@PathVariable String id) {
        String currentUserId = currentUserUtil.getCurrentUserId();

        CategoryResponse data = categoryService.findById(currentUserId,id);

        return ApiResponse.success("查詢成功", data);
    }

    /*
     * 新增使用者自訂分類
     *
     * POST /walletpet/api/categories
     *
     * 前端表單欄位：
     * categoryName
     * categoryType
     * icon，可不傳
     * color，可不傳
     *
     * 注意：
     * 不需要傳 userId。
     * userId 一律由 token 取得。
     */
    @PostMapping
    public ApiResponse<CategoryResponse> createCategory(
            @RequestParam String categoryName,
            @RequestParam CategoryType categoryType,
            @RequestParam(required = false) String icon,
            @RequestParam(required = false) String color
    ) {
        String currentUserId = currentUserUtil.getCurrentUserId();

        CategoryResponse data = categoryService.createCategory(
                currentUserId,categoryName,categoryType,icon,color);

        return ApiResponse.success("新增分類成功", data);
    }

    /*
     * 修改分類
     *
     * PUT /walletpet/api/categories/{id}
     *
     * 前端表單欄位：
     * categoryName，可不傳
     * icon，可不傳
     * color，可不傳
     * isDisable，可不傳
     *
     * 注意：
     * categoryId 建議放在 URL path。
     * 前端不需要另外傳 userId。
     */
    @PutMapping("/{id}")
    public ApiResponse<CategoryResponse> updateCategory(
            @PathVariable String id,
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) String icon,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) Boolean isDisable) {
        String currentUserId = currentUserUtil.getCurrentUserId();

        CategoryResponse data = categoryService.updateCategory(
                currentUserId,id,categoryName,icon,color,isDisable);

        return ApiResponse.success("分類更新成功", data);
    }
}