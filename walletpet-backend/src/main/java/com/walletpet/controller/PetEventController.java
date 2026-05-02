package com.walletpet.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.walletpet.dto.common.ApiResponse;
import com.walletpet.security.CurrentUserUtil;
import com.walletpet.service.PetEventService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/pets/events")
@RequiredArgsConstructor
public class PetEventController {

    private final PetEventService petEventService;

    private final CurrentUserUtil currentUserUtil;

    /*
     * 查詢目前登入者的寵物事件紀錄
     *
     * GET /walletpet/api/pets/events?page=0&size=10
     *
     * 回傳格式：
     * {
     *   "items": [
     *     {
     *       "petEventId": 1,
     *       "petName": "小咪",
     *       "eventType": "PET_FEED_CAN",
     *       "moodDelta": 1,
     *       "cancanDelta": -1,
     *       "reward": "餵食 CAN：食物量 cancan -1，心情值 mood +1",
     *       "createdAt": "2026-04-29T..."
     *     }
     *   ],
     *   "page": 0,
     *   "size": 10,
     *   "totalElements": 1,
     *   "totalPages": 1,
     *   "first": true,
     *   "last": true
     * }
     *
     * 注意：
     * 1. 前端不傳 userId。
     * 2. 前端不傳 petId。
     * 3. PetEventResponse 不回傳 userId / petId。
     */
    @GetMapping
    public ApiResponse<Map<String, Object>> getMyPetEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String currentUserId = currentUserUtil.getCurrentUserId();

        Map<String, Object> data = petEventService.getMyPetEvents(
                currentUserId,
                page,
                size
        );

        return ApiResponse.success("查詢成功", data);
    }
}