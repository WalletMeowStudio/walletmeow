package com.walletpet.service.impl;

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
import com.walletpet.entity.Pet;
import com.walletpet.entity.PetEvent;
import com.walletpet.entity.User;
import com.walletpet.entity.UserLoginLog;
<<<<<<< HEAD
=======
import com.walletpet.enums.RewardType;
>>>>>>> tzuchen
import com.walletpet.exception.BusinessException;
import com.walletpet.mapper.PetEventMapper;
import com.walletpet.mapper.PetMapper;
import com.walletpet.repository.PetEventRepository;
import com.walletpet.repository.PetRepository;
import com.walletpet.repository.UserLoginLogRepository;
import com.walletpet.repository.UserRepository;
import com.walletpet.service.PetService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class PetServiceImpl implements PetService {

    /* ========== 規則常數（小組會議定的數值，集中在這） ==========
     * mood 區間 0~100；預設值 60（首次建立寵物 / 自動回歸用）
     * cancan 下限 0、無上限
     */
    private static final int MOOD_MIN = 0;
    private static final int MOOD_MAX = 100;
    private static final int MOOD_DEFAULT = 60;

    /* 餵食基本動作 */
    private static final int BASIC_FEED_COST = 1;
    private static final int BASIC_FEED_MOOD_GAIN = 1;

    /* 大餐 */
    private static final int FEAST_COST = 10;
    private static final int FEAST_MOOD_GAIN = 15;

    /* 每日上限 */
    private static final int DAILY_FEED_MOOD_CAP = 3;        // 每日 +3 mood 上限（餵罐罐 / 小魚乾 / 零食 合計）
    private static final int DAILY_BOOKKEEPING_CANCAN_CAP = 5; // 每日 +5 cancan 上限（記帳獎勵）

    /* 登入 streak / 缺席 */
    private static final int STREAK_3_BONUS = 5;
    private static final int STREAK_7_BONUS = 10;
    private static final int MISS_3_PENALTY = -10;
    private static final int MISS_7_PENALTY = -20;

    /* event_type 約定值（與前端 EVENT_META 對齊） */
    private static final String EVT_FEED_CAN     = "FEED_CAN";
    private static final String EVT_FEED_FISH    = "FEED_FISH";
    private static final String EVT_FEED_SNACK   = "FEED_SNACK";
    private static final String EVT_FEED_FEAST   = "FEED_FEAST";
    private static final String EVT_BOOKKEEPING  = "BOOKKEEPING";
    private static final String EVT_LOGIN_STREAK = "LOGIN_STREAK";
    private static final String EVT_LOGIN_MISS   = "LOGIN_MISS";

    /** 受每日 +3 mood 上限約束的事件類型集合 */
    private static final Set<String> DAILY_CAPPED_FEED_TYPES =
            Set.of(EVT_FEED_CAN, EVT_FEED_FISH, EVT_FEED_SNACK);

    private final PetRepository petRepository;
    private final PetEventRepository petEventRepository;
    private final UserRepository userRepository;
    private final UserLoginLogRepository userLoginLogRepository;

    public PetServiceImpl(PetRepository petRepository,
                          PetEventRepository petEventRepository,
                          UserRepository userRepository,
                          UserLoginLogRepository userLoginLogRepository) {
        this.petRepository = petRepository;
        this.petEventRepository = petEventRepository;
        this.userRepository = userRepository;
        this.userLoginLogRepository = userLoginLogRepository;
    }

    /* =====================================================================
     * Pet CRUD
     * ===================================================================== */

    @Override
    public PetResponse createPet(PetCreateRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new BusinessException("使用者不存在"));

        if (petRepository.existsById(request.getPetId())) {
            throw new BusinessException("寵物 ID 已存在");
        }

        Pet pet = new Pet();
        pet.setPetId(request.getPetId());
        pet.setUser(user);
        pet.setPetName(request.getPetName());
        // 規格：第一次帳號登入軟體時預設心情值 60
        pet.setMood(clampMood(request.getMood() == null ? MOOD_DEFAULT : request.getMood()));
        pet.setCancan(clampCancan(request.getCancan() == null ? 0 : request.getCancan()));
        pet.setIsDisplayed(request.getIsDisplayed() == null ? true : request.getIsDisplayed());
        pet.setLastUpdateAt(LocalDateTime.now());
        pet.setCreatedAt(LocalDateTime.now());

        return PetMapper.toResponse(petRepository.save(pet));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PetResponse> findPetsByUserId(String userId) {
        return petRepository.findByUser_UserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(PetMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PetResponse findPetById(String petId) {
        return PetMapper.toResponse(loadPet(petId));
    }

    @Override
    public PetResponse updatePet(String petId, PetUpdateRequest request) {
        Pet pet = loadPet(petId);

        if (request.getPetName() != null && !request.getPetName().isBlank()) {
            pet.setPetName(request.getPetName());
        }
        if (request.getMood() != null) {
            pet.setMood(clampMood(request.getMood()));
        }
        if (request.getCancan() != null) {
            pet.setCancan(clampCancan(request.getCancan()));
        }
        if (request.getIsDisplayed() != null) {
            pet.setIsDisplayed(request.getIsDisplayed());
        }

        pet.setLastUpdateAt(LocalDateTime.now());
        return PetMapper.toResponse(petRepository.save(pet));
    }

    @Override
    public void deletePet(String petId) {
        petRepository.delete(loadPet(petId));
    }

    /* =====================================================================
     * Pet event 通用 CRUD
     * ===================================================================== */

    @Override
    public PetEventResponse createPetEvent(PetEventCreateRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new BusinessException("使用者不存在"));
        Pet pet = loadPet(request.getPetId());

        int moodDelta   = nullToZero(request.getMoodDelta());
        int cancanDelta = nullToZero(request.getCancanDelta());

        applyDeltas(pet, moodDelta, cancanDelta);
        PetEvent saved = saveEvent(user, pet, request.getEventType(), moodDelta, cancanDelta, request.getReward());
        petRepository.save(pet);

        return PetEventMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PetEventResponse> findPetEventsByUserId(String userId) {
        return petEventRepository.findByUser_UserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(PetEventMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PetEventResponse> findPetEventsByPetId(String petId) {
        return petEventRepository.findByPet_PetIdOrderByCreatedAtDesc(petId)
                .stream()
                .map(PetEventMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deletePetEvent(Long petEventId) {
        PetEvent event = petEventRepository.findById(petEventId)
                .orElseThrow(() -> new BusinessException("寵物事件不存在"));
        petEventRepository.delete(event);
    }

    /* =====================================================================
     * cancan 系統：餵食
     * ===================================================================== */

    @Override
    public PetResponse feed(PetFeedRequest request) {
        if (request.getFeedType() == null) {
            throw new BusinessException("feedType 不可為空");
        }
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new BusinessException("使用者不存在"));
        Pet pet = loadPet(request.getPetId());

        String type = request.getFeedType().toUpperCase();
        switch (type) {
            case "CAN":   return doBasicFeed(user, pet, EVT_FEED_CAN);
            case "FISH":  return doBasicFeed(user, pet, EVT_FEED_FISH);
            case "SNACK": return doBasicFeed(user, pet, EVT_FEED_SNACK);
            case "FEAST": return doFeastFeed(user, pet);
            default:
                throw new BusinessException("未知 feedType：" + request.getFeedType());
        }
    }

    private PetResponse doBasicFeed(User user, Pet pet, String eventType) {
        if (pet.getCancan() < BASIC_FEED_COST) {
            throw new BusinessException("罐罐不夠（需要 " + BASIC_FEED_COST + " cancan）");
        }

        // 每日 +3 mood 上限：合計 FEED_CAN/FISH/SNACK 在今天已經貢獻多少 mood
        int gainedToday = sumMoodDeltaToday(user.getUserId(), DAILY_CAPPED_FEED_TYPES);
        int allowedMoodGain = Math.max(0, DAILY_FEED_MOOD_CAP - gainedToday);
        int moodDelta = Math.min(BASIC_FEED_MOOD_GAIN, allowedMoodGain); // 達上限時 moodDelta 會 = 0

        int cancanDelta = -BASIC_FEED_COST;

        applyDeltas(pet, moodDelta, cancanDelta);
        saveEvent(user, pet, eventType, moodDelta, cancanDelta, null);
        petRepository.save(pet);
        return PetMapper.toResponse(pet);
    }

    private PetResponse doFeastFeed(User user, Pet pet) {
        if (pet.getCancan() < FEAST_COST) {
            throw new BusinessException("罐罐不夠（大餐需要 " + FEAST_COST + " cancan）");
        }
        // 大餐不受每日 +3 mood 上限約束
        int moodDelta = FEAST_MOOD_GAIN;
        int cancanDelta = -FEAST_COST;
        applyDeltas(pet, moodDelta, cancanDelta);
        saveEvent(user, pet, EVT_FEED_FEAST, moodDelta, cancanDelta, null);
        petRepository.save(pet);
        return PetMapper.toResponse(pet);
    }

    /* =====================================================================
     * cancan 系統：記帳獎勵
     * ===================================================================== */

    @Override
    public PetResponse claimBookkeepingReward(BookkeepingRewardRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new BusinessException("使用者不存在"));
        Pet pet = loadPet(request.getPetId());

        // 每日 +5 cancan 上限：今天 BOOKKEEPING 累計給了多少
        int givenToday = sumCancanDeltaToday(user.getUserId(), Set.of(EVT_BOOKKEEPING));
        int allowedCancanGain = Math.max(0, DAILY_BOOKKEEPING_CANCAN_CAP - givenToday);
        int cancanDelta = Math.min(1, allowedCancanGain); // 觸頂時 cancanDelta = 0

        applyDeltas(pet, 0, cancanDelta);

        String reward = request.getTransactionId() == null ? null : "tx:" + request.getTransactionId();
        saveEvent(user, pet, EVT_BOOKKEEPING, 0, cancanDelta, reward);
        petRepository.save(pet);

        return PetMapper.toResponse(pet);
    }

    /* =====================================================================
     * 登入 tick：套用 streak / 缺席規則
     * ===================================================================== */

    @Override
    public LoginTickResponse applyLoginTick(String userId, String petId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("使用者不存在"));
        Pet pet = loadPet(petId);

        LocalDate today = LocalDate.now();

        // 同一天重複呼叫只記錄一次，不重複加減 mood
        if (userLoginLogRepository.findByUser_UserIdAndLoginDate(userId, today).isPresent()) {
            LoginTickResponse resp = new LoginTickResponse();
            resp.setPet(PetMapper.toResponse(pet));
            resp.setStreakDays(currentStreak(userId, today));
            resp.setMissedDays(null);
            resp.setMoodDelta(0);
            resp.setAppliedRule("NONE");
            resp.setFirstLoginToday(false);
            return resp;
        }

        // 取最近 14 筆登入紀錄（多取一點以防缺席計算用到）
        List<UserLoginLog> recent = userLoginLogRepository.findTop14ByUser_UserIdOrderByLoginDateDesc(userId);

        // 缺席天數 = 今天距上一次登入相差幾天 - 1
        // 例：上次 4/24，今天 4/27 → diff 3 → missed = 2 天（4/25、4/26 沒登入）
        Integer missedDays = null;
        if (!recent.isEmpty()) {
            long diff = ChronoUnit.DAYS.between(recent.get(0).getLoginDate(), today);
            missedDays = diff <= 1 ? 0 : (int) (diff - 1);
        }

        // 寫今天的登入紀錄（要先寫，currentStreak 才能把今天算進來）
        UserLoginLog log = new UserLoginLog();
        log.setUser(user);
        log.setLoginDate(today);
        log.setCreatedAt(LocalDateTime.now());
        userLoginLogRepository.save(log);

        int streakDays = currentStreak(userId, today);

        int moodDelta = 0;
        String rule = "NONE";

        // 規則 1：缺席懲罰（缺席 7 天優先於 3 天）
        if (missedDays != null && missedDays >= 7) {
            moodDelta = MISS_7_PENALTY;
            rule = "MISS_7";
        } else if (missedDays != null && missedDays >= 3) {
            moodDelta = MISS_3_PENALTY;
            rule = "MISS_3";
        }
        // 規則 2：連續登入加成（7 連先於 3 連；只在「剛達到」時觸發以免每天重複給）
        else if (streakDays == 7) {
            // 特殊：mood < 60 時自動回歸 60，否則仍給 +10
            if (pet.getMood() < MOOD_DEFAULT) {
                moodDelta = MOOD_DEFAULT - pet.getMood();
                rule = "STREAK_7_RECOVER";
            } else {
                moodDelta = STREAK_7_BONUS;
                rule = "STREAK_7";
            }
        } else if (streakDays == 3) {
            moodDelta = STREAK_3_BONUS;
            rule = "STREAK_3";
        }

        if (moodDelta != 0) {
            applyDeltas(pet, moodDelta, 0);
            saveEvent(user, pet, rule.startsWith("MISS_") ? EVT_LOGIN_MISS : EVT_LOGIN_STREAK,
                    moodDelta, 0, rule);
            petRepository.save(pet);
        }

        LoginTickResponse resp = new LoginTickResponse();
        resp.setPet(PetMapper.toResponse(pet));
        resp.setStreakDays(streakDays);
        resp.setMissedDays(missedDays);
        resp.setMoodDelta(moodDelta);
        resp.setAppliedRule(rule);
        resp.setFirstLoginToday(true);
        return resp;
    }

    /* =====================================================================
     * 共用 helper
     * ===================================================================== */

    private Pet loadPet(String petId) {
        return petRepository.findById(petId)
                .orElseThrow(() -> new BusinessException("寵物不存在"));
    }

    private int clampMood(int v)   { return Math.max(MOOD_MIN, Math.min(MOOD_MAX, v)); }
    private int clampCancan(int v) { return Math.max(0, v); }
    private int nullToZero(Integer v) { return v == null ? 0 : v; }

    /** 套用 mood / cancan 變動量並 clamp 到合法區間 */
    private void applyDeltas(Pet pet, int moodDelta, int cancanDelta) {
        pet.setMood(clampMood(pet.getMood() + moodDelta));
        pet.setCancan(clampCancan(pet.getCancan() + cancanDelta));
        pet.setLastUpdateAt(LocalDateTime.now());
    }

    private PetEvent saveEvent(User user, Pet pet, String eventType,
                               int moodDelta, int cancanDelta, String reward) {
        PetEvent event = new PetEvent();
        event.setUser(user);
        event.setPet(pet);
        event.setEventType(eventType);
        event.setMoodDelta(moodDelta);
        event.setCancanDelta(cancanDelta);
        event.setReward(reward);
        event.setCreatedAt(LocalDateTime.now());
        return petEventRepository.save(event);
    }

    private int sumMoodDeltaToday(String userId, Set<String> eventTypes) {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end   = start.plusDays(1);
        return petEventRepository.sumMoodDeltaByUserAndTypeAndPeriod(userId, eventTypes, start, end);
    }

    private int sumCancanDeltaToday(String userId, Set<String> eventTypes) {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end   = start.plusDays(1);
        return petEventRepository.sumCancanDeltaByUserAndTypeAndPeriod(userId, eventTypes, start, end);
    }

    /**
     * 計算「今天」所在的連續登入天數。
     * 演算法：從今天往回掃 user_login_logs，遇到斷掉就停。
     */
    private int currentStreak(String userId, LocalDate today) {
        List<UserLoginLog> recent = userLoginLogRepository.findTop14ByUser_UserIdOrderByLoginDateDesc(userId);
        if (recent.isEmpty()) return 0;

        int streak = 0;
        LocalDate cursor = today;
        for (UserLoginLog log : recent) {
            if (log.getLoginDate().equals(cursor)) {
                streak++;
                cursor = cursor.minusDays(1);
            } else if (log.getLoginDate().isBefore(cursor)) {
                break;
            }
            // 若 loginDate 在 cursor 之後（不該發生但保險），略過
        }
        return streak;
    }

    @SuppressWarnings("unused")
    private LocalDateTime dayBoundary(LocalDate d) {
        return LocalDateTime.of(d, LocalTime.MIDNIGHT);
    }
<<<<<<< HEAD
=======

	@Override
	public PetResponse getMyPet(String currentUserId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PetInteractResponse interact(String currentUserId, PetInteractRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PetRewardResponse applyBookkeepingReward(String currentUserId, RewardType rewardType, Integer rewardValue,
			Integer moodDelta) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PetEventPageResponse getMyPetEvents(String currentUserId, int page, int size) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void createDefaultPetForUser(User user, String petName) {
		// TODO Auto-generated method stub
		
	}
>>>>>>> tzuchen
}
