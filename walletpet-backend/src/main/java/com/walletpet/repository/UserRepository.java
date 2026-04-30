package com.walletpet.repository;

import com.walletpet.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    
    // 根據白板需求，登入時會需要透過 userName 查詢使用者
    Optional<User> findByUserName(String userName);
    
    // 如果你想檢查帳號是否已存在
    boolean existsByUserName(String userName);
}
