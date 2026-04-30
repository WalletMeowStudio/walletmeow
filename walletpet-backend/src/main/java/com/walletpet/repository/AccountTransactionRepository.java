package com.walletpet.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.walletpet.entity.AccountTransaction;

/**
 * AccountTransactionRepository 是 account_transactions 表格的資料存取層。
 *
 * 這裡負責查詢、儲存、刪除轉帳紀錄。
 * 真正的轉帳規則，例如扣轉出帳戶、加轉入帳戶，放在 TransferServiceImpl。
 */
public interface AccountTransactionRepository extends JpaRepository<AccountTransaction, Integer> {

    /** 依建立時間由新到舊查詢使用者的轉帳紀錄。 */
    List<AccountTransaction> findByUser_UserIdOrderByCreatedAtDesc(String userId);

    /** 依轉帳日期與建立時間由新到舊查詢，適合前端列表顯示。 */
    List<AccountTransaction> findByUser_UserIdOrderByTransactionDateDescCreatedAtDesc(String userId);

    /** 用轉帳 id + 使用者 id 查單筆資料，避免使用者查到別人的轉帳紀錄。 */
    Optional<AccountTransaction> findByAccountTransIdAndUser_UserId(Integer accountTransId, String userId);
}
