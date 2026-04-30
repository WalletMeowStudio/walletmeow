package com.walletpet.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.walletpet.entity.Account;

/**
 * AccountRepository 是 accounts 表格的資料存取層。
 *
 * extends JpaRepository<Account, Integer> 之後，Spring Data JPA 會自動提供：
 * save、findById、findAll、delete 等基本 CRUD。
 *
 * 下面這些 findByUser_UserId... 是「方法命名查詢」，
 * Spring 會依照方法名稱自動產生 SQL，不需要自己寫 SELECT。
 */
public interface AccountRepository extends JpaRepository<Account, Integer> {

    /** 查詢某位使用者的全部帳戶，包含已停用帳戶。 */
    List<Account> findByUser_UserId(String userId);

    /** 查詢某位使用者尚未停用的帳戶，帳戶總覽主要使用這個方法。 */
    List<Account> findByUser_UserIdAndIsDeletedFalse(String userId);

    /** 用 accountId + userId 查單一帳戶，避免查到別人的帳戶。 */
    Optional<Account> findByAccountIdAndUser_UserId(
            Integer accountId,
            String userId
    );

    /** 用 accountId + userId 查未停用帳戶，新增交易或轉帳時會使用。 */
    Optional<Account> findByAccountIdAndUser_UserIdAndIsDeletedFalse(
            Integer accountId,
            String userId
    );

    /** 檢查同一位使用者底下是否已有同名且未停用帳戶。 */
    boolean existsByUser_UserIdAndAccountNameAndIsDeletedFalse(
            String userId,
            String accountName
    );
    
    List<Account> findByUser_UserIdAndIsSavingAccountTrueAndIsDeletedFalse(String userId);
}
