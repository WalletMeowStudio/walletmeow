package com.walletpet.controller;

<<<<<<< HEAD
import com.walletpet.dto.common.ApiResponse;
import com.walletpet.dto.pet.BookkeepingRewardRequest;
import com.walletpet.dto.pet.LoginTickResponse;
import com.walletpet.dto.pet.PetCreateRequest;
import com.walletpet.dto.pet.PetFeedRequest;
import com.walletpet.dto.pet.PetResponse;
import com.walletpet.dto.pet.PetUpdateRequest;
import com.walletpet.service.PetService;

=======
>>>>>>> tzuchen
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.walletpet.dto.common.ApiResponse;
import com.walletpet.dto.pet.BookkeepingRewardRequest;
import com.walletpet.dto.pet.LoginTickResponse;
import com.walletpet.dto.pet.PetCreateRequest;
import com.walletpet.dto.pet.PetFeedRequest;
import com.walletpet.dto.pet.PetInteractRequest;
import com.walletpet.dto.pet.PetInteractResponse;
import com.walletpet.dto.pet.PetResponse;
import com.walletpet.dto.pet.PetUpdateRequest;
import com.walletpet.security.CurrentUserUtil;
import com.walletpet.service.PetService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/pets")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;

    private final CurrentUserUtil currentUserUtil;

    // 查詢目前登入者的寵物狀態
    // GET http://localhost:8080/walletpet/api/pets/me
    @GetMapping("/me")
    public ApiResponse<PetResponse> getMyPet() {
        String currentUserId = currentUserUtil.getCurrentUserId();

        PetResponse data = petService.getMyPet(currentUserId);

        return ApiResponse.success("查詢成功", data);
    }

    // 寵物互動
    // POST http://localhost:8080/walletpet/api/pets/interact
    @PostMapping("/interact")
    public ApiResponse<PetInteractResponse> interact(
            @RequestBody PetInteractRequest request
    ) {
        String currentUserId = currentUserUtil.getCurrentUserId();

        PetInteractResponse data = petService.interact(
                currentUserId,
                request
        );

        return ApiResponse.success("互動成功", data);
    }
    


    @PostMapping
    public ApiResponse<PetResponse> create(@RequestBody PetCreateRequest request) {
        return new ApiResponse<>(true, "新增成功", petService.createPet(request));
    }

    @GetMapping
    public ApiResponse<List<PetResponse>> findByUserId(@RequestParam String userId) {
        return new ApiResponse<>(true, "查詢成功", petService.findPetsByUserId(userId));
    }

    @GetMapping("/{petId}")
    public ApiResponse<PetResponse> findById(@PathVariable String petId) {
        return new ApiResponse<>(true, "查詢成功", petService.findPetById(petId));
    }

    @PutMapping("/{petId}")
    public ApiResponse<PetResponse> update(@PathVariable String petId,
                                           @RequestBody PetUpdateRequest request) {
        return new ApiResponse<>(true, "修改成功", petService.updatePet(petId, request));
    }

    @DeleteMapping("/{petId}")
    public ApiResponse<Object> delete(@PathVariable String petId) {
        petService.deletePet(petId);
        return new ApiResponse<>(true, "刪除成功", null);
    }

    /* ========== cancan 系統端點 ========== */

    /**
     * 餵食寵物：feedType = CAN / FISH / SNACK / FEAST
     * 規則參考 PetServiceImpl 常數段。
     */
    @PostMapping("/feed")
    public ApiResponse<PetResponse> feed(@RequestBody PetFeedRequest request) {
        return new ApiResponse<>(true, "餵食成功", petService.feed(request));
    }

    /**
     * 記帳獎勵：每次記帳後呼叫一次（前端在 transaction.create 成功後接續呼叫）。
     * 同一天最多累計 +5 cancan，超過則 cancanDelta = 0（仍視為成功，前端可顯示「今日已達上限」）。
     */
    @PostMapping("/claim-bookkeeping")
    public ApiResponse<PetResponse> claimBookkeeping(@RequestBody BookkeepingRewardRequest request) {
        return new ApiResponse<>(true, "記帳獎勵已套用", petService.claimBookkeepingReward(request));
    }

    /**
     * 登入 tick：登入後（或進入 dashboard）呼叫一次。
     * 同一天重複呼叫不會重複加減，回傳 firstLoginToday=false。
     * 透過 query param 帶 userId & petId。
     */
    @PostMapping("/login-tick")
    public ApiResponse<LoginTickResponse> loginTick(@RequestParam String userId,
                                                    @RequestParam String petId) {
        return new ApiResponse<>(true, "登入 tick 已套用", petService.applyLoginTick(userId, petId));
    }
<<<<<<< HEAD
}
=======
    
}
>>>>>>> tzuchen
