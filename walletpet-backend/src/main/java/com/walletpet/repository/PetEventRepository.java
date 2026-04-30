package com.walletpet.repository;

import com.walletpet.entity.PetEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface PetEventRepository extends JpaRepository<PetEvent, Long> {

    List<PetEvent> findByUser_UserIdOrderByCreatedAtDesc(String userId);

    List<PetEvent> findByPet_PetIdOrderByCreatedAtDesc(String petId);

    /**
     * 加總指定使用者在一段時間區間內、特定 event_type 集合的 mood_delta。
     * 用於每日 +3 mood 上限稽核（FEED_CAN / FEED_FISH / FEED_SNACK 合計）。
     * 若沒有任何符合的紀錄回傳 0。
     */
    @Query("""
            SELECT COALESCE(SUM(e.moodDelta), 0)
            FROM PetEvent e
            WHERE e.user.userId = :userId
              AND e.eventType IN :eventTypes
              AND e.createdAt >= :startInclusive
              AND e.createdAt <  :endExclusive
            """)
    int sumMoodDeltaByUserAndTypeAndPeriod(
            @Param("userId") String userId,
            @Param("eventTypes") Collection<String> eventTypes,
            @Param("startInclusive") LocalDateTime startInclusive,
            @Param("endExclusive") LocalDateTime endExclusive
    );

    /**
     * 加總指定使用者在一段時間區間內、特定 event_type 集合的 cancan_delta。
     * 用於每日 +5 cancan 上限稽核（BOOKKEEPING）。
     * 因為 cancan_delta 紀錄的是「對寵物 cancan 餘額的變化」（記帳獎勵為 +1），
     * 這裡回傳值代表當日已發出的累計獎勵量。
     */
    @Query("""
            SELECT COALESCE(SUM(e.cancanDelta), 0)
            FROM PetEvent e
            WHERE e.user.userId = :userId
              AND e.eventType IN :eventTypes
              AND e.createdAt >= :startInclusive
              AND e.createdAt <  :endExclusive
            """)
    int sumCancanDeltaByUserAndTypeAndPeriod(
            @Param("userId") String userId,
            @Param("eventTypes") Collection<String> eventTypes,
            @Param("startInclusive") LocalDateTime startInclusive,
            @Param("endExclusive") LocalDateTime endExclusive
    );
<<<<<<< HEAD
}
=======
}
>>>>>>> tzuchen
