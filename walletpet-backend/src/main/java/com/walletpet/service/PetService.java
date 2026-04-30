package com.walletpet.service;

<<<<<<< HEAD
=======
import java.util.List;

>>>>>>> tzuchen
import com.walletpet.dto.pet.BookkeepingRewardRequest;
import com.walletpet.dto.pet.LoginTickResponse;
import com.walletpet.dto.pet.PetCreateRequest;
import com.walletpet.dto.pet.PetEventCreateRequest;
import com.walletpet.dto.pet.PetEventPageResponse;
import com.walletpet.dto.pet.PetEventResponse;
import com.walletpet.dto.pet.PetFeedRequest;
<<<<<<< HEAD
=======
import com.walletpet.dto.pet.PetInteractRequest;
import com.walletpet.dto.pet.PetInteractResponse;
>>>>>>> tzuchen
import com.walletpet.dto.pet.PetResponse;
import com.walletpet.dto.pet.PetRewardResponse;
import com.walletpet.dto.pet.PetUpdateRequest;
import com.walletpet.entity.User;
import com.walletpet.enums.RewardType;


public interface PetService {

    PetResponse createPet(PetCreateRequest request);

    List<PetResponse> findPetsByUserId(String userId);

    PetResponse findPetById(String petId);

    PetResponse updatePet(String petId, PetUpdateRequest request);

    void deletePet(String petId);

    PetEventResponse createPetEvent(PetEventCreateRequest request);

    List<PetEventResponse> findPetEventsByUserId(String userId);

    List<PetEventResponse> findPetEventsByPetId(String petId);

    void deletePetEvent(Long petEventId);
<<<<<<< HEAD

=======
	
	PetResponse getMyPet(String currentUserId);

    PetInteractResponse interact(String currentUserId,PetInteractRequest request);

    PetRewardResponse applyBookkeepingReward(
            String currentUserId,RewardType rewardType,Integer rewardValue,Integer moodDelta);

    PetEventPageResponse getMyPetEvents(String currentUserId,int page,int size);

    void createDefaultPetForUser(User user,String petName);
>>>>>>> tzuchen
    /* ========== cancan 系統 ========== */

    /**
     * 餵食寵物。依 feedType 套用 cancan 消耗、心情值增量與每日上限。
     * feedType: CAN / FISH / SNACK / FEAST
     */
    PetResponse feed(PetFeedRequest request);

    /**
     * 記帳獎勵：呼叫一次給寵物 cancan +1（同一天最多 +5）。
     */
    PetResponse claimBookkeepingReward(BookkeepingRewardRequest request);

    /**
     * 登入 tick：每天首次登入呼叫一次，套用 streak / 缺席規則並更新 mood。
     */
    LoginTickResponse applyLoginTick(String userId, String petId);
<<<<<<< HEAD
}
=======
    
	/*
    PetResponse createPet(PetCreateRequest request);

    List<PetResponse> findPetsByUserId(String userId);

    PetResponse findPetById(String petId);

    PetResponse updatePet(String petId, PetUpdateRequest request);

    void deletePet(String petId);

    PetEventResponse createPetEvent(PetEventCreateRequest request);

    List<PetEventResponse> findPetEventsByUserId(String userId);

    List<PetEventResponse> findPetEventsByPetId(String petId);

    void deletePetEvent(Long petEventId);*/
}
>>>>>>> tzuchen
