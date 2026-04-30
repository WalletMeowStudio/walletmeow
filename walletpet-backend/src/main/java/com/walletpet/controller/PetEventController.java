package com.walletpet.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.walletpet.dto.common.ApiResponse;
import com.walletpet.dto.pet.PetEventCreateRequest;
import com.walletpet.dto.pet.PetEventPageResponse;
import com.walletpet.dto.pet.PetEventResponse;
import com.walletpet.security.CurrentUserUtil;
import com.walletpet.service.PetService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/pets/events")
@RequiredArgsConstructor
public class PetEventController {

    private final PetService petService;

    private final CurrentUserUtil currentUserUtil;

    // 查詢目前登入者的寵物事件紀錄，支援分頁
    // GET http://localhost:8080/walletpet/api/pets/events?page=0&size=10
    @GetMapping("/pageing")
    public ApiResponse<PetEventPageResponse> getMyPetEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String currentUserId = currentUserUtil.getCurrentUserId();

        PetEventPageResponse data = petService.getMyPetEvents(
                currentUserId,
                page,
                size
        );

        return ApiResponse.success("查詢成功", data);
    }
    
    @PostMapping
    public ApiResponse<PetEventResponse> create(@RequestBody PetEventCreateRequest request) {
        return new ApiResponse<>(true, "新增成功", petService.createPetEvent(request));
    }

    @GetMapping
    public ApiResponse<List<PetEventResponse>> findByUserId(@RequestParam String userId) {
        return new ApiResponse<>(true, "查詢成功", petService.findPetEventsByUserId(userId));
    }

    @GetMapping("/pet/{petId}")
    public ApiResponse<List<PetEventResponse>> findByPetId(@PathVariable String petId) {
        return new ApiResponse<>(true, "查詢成功", petService.findPetEventsByPetId(petId));
    }

    @DeleteMapping("/{petEventId}")
    public ApiResponse<Object> delete(@PathVariable Long petEventId) {
        petService.deletePetEvent(petEventId);
        return new ApiResponse<>(true, "刪除成功", null);
    }
}