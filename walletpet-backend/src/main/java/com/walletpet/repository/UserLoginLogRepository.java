package com.walletpet.repository;

import com.walletpet.entity.UserLoginLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserLoginLogRepository extends JpaRepository<UserLoginLog, Long> {

    Optional<UserLoginLog> findByUser_UserIdAndLoginDate(String userId, LocalDate loginDate);

    /**
     * 取出指定使用者最近 N 筆登入紀錄，依日期由新到舊排序，
     * 用於計算連續登入 / 缺席天數。
     */
    List<UserLoginLog> findTop14ByUser_UserIdOrderByLoginDateDesc(String userId);
}
