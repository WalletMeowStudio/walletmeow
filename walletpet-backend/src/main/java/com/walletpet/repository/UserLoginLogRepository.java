package com.walletpet.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.walletpet.entity.UserLoginLog;

public interface UserLoginLogRepository extends JpaRepository<UserLoginLog, Long> {

    boolean existsByUser_UserIdAndLoginDate(
            String userId,
            LocalDate loginDate
    );

    Optional<UserLoginLog> findByUser_UserIdAndLoginDate(
            String userId,
            LocalDate loginDate
    );

    Optional<UserLoginLog> findTop1ByUser_UserIdAndLoginDateBeforeOrderByLoginDateDesc(
            String userId,
            LocalDate loginDate
    );

    List<UserLoginLog> findByUser_UserIdAndLoginDateBetweenOrderByLoginDateDesc(
            String userId,
            LocalDate startDate,
            LocalDate endDate
    );
}