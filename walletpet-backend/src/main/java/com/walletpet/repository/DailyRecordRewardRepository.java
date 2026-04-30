package com.walletpet.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.walletpet.entity.DailyRecordReward;

public interface DailyRecordRewardRepository extends JpaRepository<DailyRecordReward, Long> {

    Optional<DailyRecordReward> findByUser_UserIdAndRewardDate(String userId,LocalDate rewardDate);

    List<DailyRecordReward> findTop30ByUser_UserIdOrderByRewardDateDesc(String userId);
}