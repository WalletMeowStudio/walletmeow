# WalletPet 記帳遊戲需求文件（SRS + SA + SD + API）

> 本文件依據目前專案版本整理，內容包含需求規格（SRS）、系統分析（SA）、系統設計（SD）與 API 對接規格。本文以目前後端與前端實作為準，管理員功能保留於規劃範圍內，尚未列為已完成項目。

---

# 1. 專案摘要

WalletPet 是一套結合理財記帳與寵物互動的系統。使用者可管理收入、支出、轉帳、帳戶、分類、預算與存錢目標；系統會依照使用者的記帳、登入與餵食行為，更新寵物的心情值 `mood` 與食物量 `cancan`，並透過事件紀錄、動畫狀態與提示訊息提升持續記帳動機。

目前專案以一般使用者功能為主要實作範圍，包含登入註冊、帳戶管理、分類管理、交易管理、轉帳、預算、存錢目標、寵物狀態、餵食、每日記帳獎勵與登入 streak。管理員分析功能目前保留於架構設計中，後端已有部分 DTO 與 Service 介面雛形，尚未完成 Controller 與完整商業邏輯。

---

# 2. SRS：需求規格說明書

## 2.1 系統目標

1. 提供使用者記錄收入、支出、轉帳與查詢明細的個人理財系統。
2. 提供資產帳戶、分類、預算與存錢目標管理功能。
3. 透過寵物心情值 `mood`、食物量 `cancan`、餵食、每日記帳獎勵與登入 streak 建立遊戲化回饋。
4. 提供圖表與儀表板摘要，使使用者掌握收支狀況。
5. 保留管理員分析架構，作為後續查看用戶消費習慣與測試寵物動畫狀態之用。

## 2.2 系統範圍

| 範圍類別 | 內容 |
|---|---|
| 目前主要實作 | 登入、註冊、目前使用者資料、帳戶管理、帳戶摘要、分類管理、交易新增 / 查詢 / 修改 / 刪除、轉帳、預算、存錢目標、寵物狀態、餵食、登入 tick、每日記帳獎勵、寵物事件紀錄、圖表 summary。 |
| 已保留但未完整實作 | 管理員用戶消費總覽、單一用戶詳細分析、群體分析、管理員寵物動畫測試。 |
| 後續擴充 | 發票掃描自動記帳、推播通知、更多寵物模型、正式後台管理頁、更多圖表 API。 |

## 2.3 使用者角色

| 角色 | 說明 | 主要權限 |
|---|---|---|
| 一般使用者 | 系統主要使用者 | 管理個人帳戶、分類、交易、轉帳、預算、存錢目標、寵物狀態與獎勵。 |
| 管理員 | 後台管理角色，目前保留規劃 | 後續可查看用戶消費統計、群體分析、管理測試資料與寵物動畫狀態。 |
| 專案團隊 | 開發與維護人員 | 依模組維護前端頁面、API、Service、Repository 與資料表。 |

## 2.4 功能需求

| 編號 | 功能名稱 | 需求描述 |
|---|---|---|
| FR-01 | 登入 | 使用者以帳號密碼登入，成功後取得 token。 |
| FR-02 | 註冊 | 使用者註冊時可填寫 `userName`、`password`、`petName`；後端建立使用者與初始資料。 |
| FR-03 | 目前使用者資料 | 前端可透過 token 取得目前登入者。 |
| FR-04 | 帳戶管理 | 使用者可新增、查詢、修改、停用帳戶。 |
| FR-05 | 帳戶摘要 | 顯示總資產、總負債、淨資產。 |
| FR-06 | 存款帳戶查詢 | 存錢目標可查詢 `isSavingAccount=true` 的帳戶。 |
| FR-07 | 分類管理 | 使用者可查詢、新增、編輯、停用收入 / 支出分類。 |
| FR-08 | 可用分類查詢 | 新增交易頁只撈啟用分類。 |
| FR-09 | 新增收入 / 支出 | 使用者可新增交易並同步更新帳戶餘額。 |
| FR-10 | 交易查詢 | 使用者可依日期、帳戶、分類、型別、分頁查詢交易。 |
| FR-11 | 交易修改 / 刪除 | 修改或刪除交易時需回沖舊帳戶影響並重新計算。 |
| FR-12 | 交易摘要 | 依條件統計收入、支出、結餘與筆數。 |
| FR-13 | 轉帳 | 使用者可建立帳戶間轉帳。 |
| FR-14 | 轉帳查詢 / 刪除 | 查詢轉帳列表、單筆轉帳與刪除轉帳。 |
| FR-15 | 預算管理 | 建立、查詢、修改金額、刪除預算，並計算目前使用進度。 |
| FR-16 | 存錢目標 | 建立、查詢、存入、完成、刪除存錢目標。 |
| FR-17 | 圖表摘要 | 首頁或分析頁可讀取收支 summary。 |
| FR-18 | 寵物狀態 | 查詢目前登入者顯示中的寵物。 |
| FR-19 | 餵食 | 使用者用 cancan 餵食寵物，更新 mood / cancan。 |
| FR-20 | 登入 streak | 使用者登入後呼叫 login-tick，依登入紀錄更新 mood。 |
| FR-21 | 每日記帳獎勵 | 新增交易後計算每日 cancan 獎勵。 |
| FR-22 | 寵物事件 | 查詢餵食、登入、記帳獎勵等事件紀錄。 |
| FR-23 | 管理員用戶總覽 | 管理員查看每位用戶收支與記帳摘要。(待實作) |
| FR-24 | 管理員用戶詳細分析 | 管理員查看單一用戶分類、帳戶、預算、目標與寵物趨勢。(待實作) |
| FR-25 | 管理員群體分析 | 管理員查看全體用戶消費習慣。(待實作) |
| FR-26 | 管理員寵物動畫測試 | 管理員手動調整 mood 測試動畫。(待實作) |

## 2.5 非功能需求

| 編號 | 類別 | 需求內容 |
|---|---|---|
| NFR-01 | 安全性 | 除登入與註冊外，API 原則上需從 token 取得 `currentUserId`，不由前端傳入 `userId` 決定資料歸屬。 |
| NFR-02 | 一致性 | API 回傳統一使用 `ApiResponse<T>`：`success`、`message`、`data`。目前沒有 `errorCode` 欄位。 |
| NFR-03 | 維護性 | 後端依 Controller / Service / Repository / Entity / DTO / Mapper 分層。 |
| NFR-04 | 資料正確性 | 交易與轉帳會影響帳戶餘額，修改 / 刪除時需回沖舊資料。 |
| NFR-05 | 冪等性 | 每日記帳獎勵與每日登入紀錄需避免同日重複發放。 |
| NFR-06 | 擴充性 | 管理員、發票掃描、更多圖表與更多寵物模型應可在既有資料結構上擴充。 |

---

# 3. SA：系統分析

## 3.1 主要使用情境（Use Cases）

| 編號 | 使用情境 | 角色 | 主要流程 | 替代流程 / 例外 | 對應需求 |
|---|---|---|---|---|---|
| UC-01 | 註冊 | 使用者 | 輸入帳號、密碼、寵物名稱 → 建立 User → 建立預設資料與初始寵物。 | 帳號重複 → 回傳錯誤。 | FR-02 |
| UC-02 | 登入 | 使用者 | 輸入帳密 → 後端驗證 → 回傳 token → 前端保存 token。 | 帳密錯誤 → 顯示錯誤。 | FR-01 |
| UC-03 | 取得目前使用者 | 使用者 | 前端帶 Authorization header → 後端回傳 userId、userName、role。 | token 無效 → 禁止存取。 | FR-03 |
| UC-04 | 查看帳戶 | 使用者 | 進入帳戶頁 → 查詢帳戶列表與帳戶摘要。 | 無帳戶 → 顯示空狀態或預設帳戶。 | FR-04、FR-05 |
| UC-05 | 新增帳戶 | 使用者 | 輸入帳戶名稱、初始餘額、是否負債、是否存款帳戶 → 儲存。 | 名稱空白 → 前端阻擋。 | FR-04 |
| UC-06 | 修改 / 停用帳戶 | 使用者 | 選取帳戶 → 暫存 accountId → 修改或停用。 | 帳戶不存在或不屬於本人 → 後端拒絕。 | FR-04 |
| UC-07 | 管理分類 | 使用者 | 切換收入 / 支出頁籤 → 新增、編輯、停用分類。 | 系統分類限制修改時需由 Service 判斷。 | FR-07 |
| UC-08 | 新增收入 / 支出 | 使用者 | 選擇交易類型、帳戶、分類、金額、日期、備註 → 新增交易 → 更新帳戶與獎勵。 | 帳戶 / 分類停用、金額不合法 → 拒絕。 | FR-09、FR-21 |
| UC-09 | 查詢交易 | 使用者 | 依日期、帳戶、分類、型別、分頁查詢交易與摘要。 | 無資料 → 回傳空 items 與 summary 0。 | FR-10、FR-12 |
| UC-10 | 修改 / 刪除交易 | 使用者 | 暫存 transactionId → 修改或刪除 → 後端回沖帳戶餘額。 | 日期變更需重算舊日期與新日期每日獎勵。 | FR-11 |
| UC-11 | 新增轉帳 | 使用者 | 選擇來源帳戶、目標帳戶、金額、日期 → 寫入轉帳紀錄。 | 來源與目標相同、餘額不足 → 拒絕。 | FR-13 |
| UC-12 | 預算管理 | 使用者 | 建立預算 → 查詢預算進度 → 修改金額或刪除。 | 目前直接回傳 Budget Entity。 | FR-15 |
| UC-13 | 存錢目標 | 使用者 | 建立目標 → 存入金額 → 完成或刪除目標。 | 存入來源帳戶不足、刪除轉回帳戶不存在 → 拒絕。 | FR-16 |
| UC-14 | 查看寵物 | 使用者 | 查詢 `/api/pets/me` → 顯示 petName、mood、cancan、模型。 | 無顯示寵物 → 後端建立或回錯。 | FR-18 |
| UC-15 | 餵食 | 使用者 | 選 foodType → 呼叫 feed → 扣 cancan、加 mood、寫入事件。 | cancan 不足 → 拒絕。 | FR-19、FR-22 |
| UC-16 | 登入 tick | 使用者 | 登入後呼叫 login-tick → 寫入登入紀錄 → 計算 streak / missedDays。 | 今日已登入 → 不重複加減 mood。 | FR-20 |
| UC-17 | 查看每日獎勵 | 使用者 | 查詢今日 / 歷史 reward → 顯示交易筆數與 cancanDelta。 | 無紀錄 → 依 Service 規則回傳預設或 null。 | FR-21 |
| UC-18 | 查看寵物事件 | 使用者 | 查詢 `/api/pets/events?page=0&size=10` → 顯示事件列表。 | 無事件 → 空列表。 | FR-22 |
| UC-19 | 查看圖表摘要 | 使用者 | 查詢 `/api/charts/summary` → 顯示月收入、月支出與最近交易。 | 目前 ChartController 暫用 default user，需改為 currentUserId。 | FR-17 |
| UC-20 | 管理員用戶總覽 | 管理員 | 進入後台 → 查詢所有使用者消費摘要。 | 尚未完成 Controller。 | FR-23 |
| UC-21 | 管理員詳細分析 | 管理員 | 點選單一用戶 → 查詢分類、帳戶、預算、目標、寵物趨勢。 | 尚未完成 Controller / DTO。 | FR-24 |
| UC-22 | 管理員群體分析 | 管理員 | 查詢全體用戶消費習慣與活躍度。 | 尚未完成。 | FR-25 |
| UC-23 | 管理員動畫測試 | 管理員 | 調整指定寵物 mood → 驗證動畫。 | 尚未完成。 | FR-26 |

## 3.2 核心流程

### 流程 A：登入與寵物 streak

登入表單 → `POST /api/auth/login` → 回傳 token → 前端保存 token → 呼叫 `POST /api/pets/login-tick` → LoginStreakService 寫入 `user_login_logs` → 計算 `loginStreakDays`、`missedDays`、`moodDelta` → 更新 `pets` → 寫入 `pet_events` → 回傳 `LoginTickResponse`。

### 流程 B：新增交易與每日記帳獎勵

交易表單 → 前端組成 `TransactionCreateRequest` → `POST /api/transactions` → TransactionService 寫入交易 → 更新帳戶餘額 → 呼叫 DailyRewardService 計算指定日期 → 更新 `daily_record_rewards`、`pets.cancan` → 寫入 `pet_events` → 前端可比對新增前後 `cancanDelta` 顯示記帳獎勵提示。

### 流程 C：分類管理

使用者點選 icon → 前端保存目前選取 icon → 使用者切換收入 / 支出頁籤 → 前端更新目前分類類型 → 送出分類表單 → `categoryApi.create()` 使用 form params 傳 `categoryName`、`categoryType`、`icon`、`color` → 後端 `CategoryController` 以 `@RequestParam` 接收。

### 流程 D：轉帳

轉帳表單 → `TransferCreateRequest` → TransferService 檢查帳戶歸屬與帳戶狀態 → 來源帳戶扣款 → 目標帳戶加款 → 寫入 `account_transactions` → 回傳 `TransferResponse`。

---

# 4. SD：系統設計

## 4.1 分層架構

| 層級 | 責任 |
|---|---|
| Frontend | HTML / CSS / JavaScript 頁面、表單資料組合、前端狀態暫存、API 呼叫、圖表與動畫顯示。 |
| Controller | 接收 request body、query param、path variable；取得 currentUserId；呼叫 Service；回傳 ApiResponse。 |
| Service | 商業邏輯、資料歸屬檢查、帳戶餘額更新、獎勵計算、寵物規則、預算與目標運算。 |
| Repository | Spring Data JPA 查詢與 CRUD。 |
| Entity | 對應資料表。 |
| DTO / Mapper | 控制前後端 request / response 欄位，避免直接暴露 Entity 關聯。 |
| Database | 儲存 users、accounts、categories、transactions、account_transactions、budget、saving_goals、pets、pet_events、daily_record_rewards、user_login_logs、pet_model。 |

## 4.2 前端頁面與後端對應

| 前端頁面 | 對應 API 模組 | 主要後端資料 |
|---|---|---|
| `login.html` | Auth、User、Pet login tick | users、user_login_logs、pets、pet_events |
| `dashboard.html` | User、Account、Transaction、Pet、Chart | users、accounts、transactions、pets |
| `accounts.html` | Account | accounts |
| `categories.html` | Category | categories |
| `transactions.html` | Transaction、Category、Account、DailyReward | transactions、accounts、categories、daily_record_rewards、pets、pet_events |
| `transfers.html` | Transfer、Account | account_transactions、accounts |
| `goals.html` | SavingGoal、Budget、Account | saving_goals、budget、accounts、transactions |
| `analytics.html` | Chart、Transaction | transactions、categories、accounts |
| `pets.html` | Pet、DailyReward、PetEvent | pets、pet_events、daily_record_rewards、user_login_logs |
| Admin pages | AdminAnalysis、AdminPetTest | 尚未建立前端頁面，保留規劃。 |

## 4.3 後端模組清單

| 模組 | Controller | Service | Repository | DTO / Response |
|---|---|---|---|---|
| Auth | `AuthController` | `AuthService` / `AuthServiceImpl` | `UserRepository` | `LoginRequest`, `LoginResponse` |
| User | `UserController` | `UserService` / `UserServiceImpl` | `UserRepository` | `UserRegisterRequest`, `UserUpdateRequest`, `UserResponse` |
| Account | `AccountController` | `AccountService` / `AccountServiceImpl` | `AccountRepository` | `AccountCreateRequest`, `AccountUpdateRequest`, `AccountResponse`, `AccountSummaryResponse` |
| Category | `CategoryController` | `CategoryService` / `CategoryServiceImpl` | `CategoryRepository` | `CategoryResponse` |
| Transaction | `TransactionController` | `TransactionService` / `TransactionServiceImpl` | `TransactionRepository` | `TransactionCreateRequest`, `TransactionUpdateRequest`, `TransactionResponse` |
| Transfer | `TransferController` | `TransferService` / `TransferServiceImpl` | `AccountTransactionRepository` | `TransferCreateRequest`, `TransferResponse`, `TransferAccountBalanceResponse` |
| Budget | `BudgetController` | `BudgetService` / `BudgetServiceImpl` | `BudgetRepository` | `BudgetResult`，目前部分 API 直接使用 `Budget` Entity |
| SavingGoal | `SavingGoalController` | `SavingGoalService` / `SavingGoalServiceImpl` | `SavingGoalRepository` | `SavingGoalRequest`，目前 response 直接回傳 `SavingGoal` Entity |
| Pet | `PetController` | `PetService`, `LoginStreakService` | `PetRepository`, `PetModelRepository`, `UserLoginLogRepository` | `PetResponse`, `LoginTickResponse` |
| PetEvent | `PetEventController` | `PetEventService` | `PetEventRepository` | `PetEventResponse` |
| DailyReward | `DailyRewardController` | `DailyRewardService` | `DailyRecordRewardRepository` | `DailyRewardResponse` |
| Chart | `ChartController` | `TransactionService` | `TransactionRepository` | `Map<String,Object>` |
| AdminAnalysis | 尚未建立 | `AdminAnalyticsService` 目前為空介面 | 待補 | `AdminConsumptionSummaryDto` |

## 4.4 資料表設計重點

| 資料表 | 主鍵 | 重要欄位 | 用途 |
|---|---|---|---|
| `users` | `user_id` | `user_name`, `password`, `role`, `created_at` | 使用者與權限。 |
| `accounts` | `account_id` | `user_id`, `account_name`, `balance`, `is_liability`, `is_deleted`, `is_saving_account`, `created_at` | 資產帳戶與餘額。 |
| `categories` | `category_id` | `user_id`, `category_name`, `category_type`, `icon`, `color`, `is_system`, `is_disable`, `created_at` | 收入 / 支出分類。 |
| `transactions` | `transaction_id` | `user_id`, `account_id`, `category_id`, `transaction_amount`, `transaction_type`, `transaction_date`, `note`, `created_at` | 收入 / 支出交易。 |
| `account_transactions` | `account_trans_id` | `user_id`, `from_account_id`, `to_account_id`, `transaction_amount`, `transaction_date`, `note`, `created_at` | 轉帳紀錄。 |
| `budget` | `budget_id` | `user_id`, `budget_scope`, `category_id`, `target_type`, `budget_amount`, `start_date`, `end_date`, `created_at` | 預算設定。 |
| `saving_goals` | `saving_goal_id` | `goal_name`, `target_amount`, `final_account_name`, `final_amount`, `start_date`, `end_date`, `user_id`, `account_id`, `status`, `created_at` | 存錢目標。 |
| `pet_model` | `petmodel_id` | `rive_name`, `description` | 寵物模型。 |
| `pets` | `pet_id` | `user_id`, `pet_name`, `mood`, `cancan`, `last_update_at`, `is_displayed`, `created_at`, `model_id` | 寵物目前狀態。 |
| `pet_events` | `pet_event_id` | `user_id`, `pet_id`, `event_type`, `mood_delta`, `cancan_delta`, `reward`, `created_at` | 寵物事件流水帳。 |
| `daily_record_rewards` | `daily_reward_id` | `user_id`, `reward_date`, `qualified`, `transaction_count`, `streak_days`, `reward_type`, `reward_value`, `mood_delta`, `cancan_delta`, `claimed_at`, `created_at`, `updated_at` | 每日記帳獎勵。 |
| `user_login_logs` | `login_log_id` | `user_id`, `login_date`, `created_at` | 每日登入紀錄與 streak 計算。 |

---

# 5. API 詳細規格

## 5.1 共用規則

| 項目 | 說明 |
|---|---|
| Base URL | 本機測試含 context path：`http://localhost:8080/walletpet`。 |
| Header | 除登入與註冊外，原則上使用 `Authorization: Bearer {token}`。 |
| 回傳格式 | `ApiResponse<T>`：`{ "success": true, "message": "...", "data": ... }`。目前沒有 `errorCode` 欄位。 |
| userId | 一般使用者 API 不由前端傳入 userId，由後端 token 解析。 |
| Path id | 編輯 / 刪除時由前端 state 暫存 id，送出時放入 path。 |

## 5.2 Auth / User API

| 模組 | Method | API | 傳入值 | 回傳值 | 用途 | 前端注意 | 對應 UC |
|---|---|---|---|---|---|---|---|
| Auth | POST | `/api/auth/login` | Body JSON：`{ userName, password }` | `ApiResponse<LoginResponse>`：`userId`, `userName`, `role`, `token` | 使用者登入 | 登入成功後保存 token；可接續呼叫 `/api/pets/login-tick`。 | UC-02 |
| User | POST | `/api/users/register` | Body JSON：`{ userName, password, petName }` | `ApiResponse<UserResponse>`：`userId`, `userName`, `role`, `createdAt` | 註冊使用者並建立初始資料 | `petName` 由註冊表單帶入，用於建立初始寵物。 | UC-01 |
| User | GET | `/api/users/me` | Header：Authorization | `ApiResponse<UserResponse>` | 取得目前登入者 | 頁面載入時可用於確認登入狀態與角色。 | UC-03 |
| User | PUT | `/api/users/me` | Header；Body JSON：`{ userName, password }` | `ApiResponse<UserResponse>` | 修改目前使用者資料 | 不傳 userId；後端由 token 判斷目前使用者。 | UC-03 |

## 5.3 Account API

| 模組 | Method | API | 傳入值 | 回傳值 | 用途 | 前端注意 | 對應 UC |
|---|---|---|---|---|---|---|---|
| Account | GET | `/api/accounts` | Header；Query 可選：`includeDeleted=true/false` | `ApiResponse<List<AccountResponse>>` | 查詢帳戶列表 | 前端若要包含停用帳戶，需帶 includeDeleted 或依目前 API 實作調整參數名稱。 | UC-04 |
| Account | POST | `/api/accounts` | Header；Body JSON：`{ accountName, initialBalance, isLiability, isSavingAccount }` | `ApiResponse<AccountResponse>` | 新增帳戶 | 新增欄位應送 `initialBalance`，不應送 `balance`。 | UC-05 |
| Account | GET | `/api/accounts/{id}` | Header；Path：`id` | `ApiResponse<AccountResponse>` | 查詢單一帳戶 | id 由帳戶清單取得。 | UC-04、UC-06 |
| Account | PUT | `/api/accounts/{id}` | Header；Path：`id`；Body JSON：`{ accountName, isLiability, isSavingAccount }` | `ApiResponse<AccountResponse>` | 修改帳戶 | 修改帳戶不直接送 balance。 | UC-06 |
| Account | DELETE | `/api/accounts/{id}` | Header；Path：`id` | `ApiResponse<AccountResponse>` | 停用帳戶 | 回傳欄位為 `isDeleted`。 | UC-06 |
| Account | GET | `/api/accounts/summary` | Header | `ApiResponse<AccountSummaryResponse>`：`totalAssets`, `totalLiabilities`, `netWorth` | 查詢帳戶摘要 | 帳戶頁與 dashboard 可使用。 | UC-04 |
| Account | GET | `/api/accounts/saving-only` | Header | `ApiResponse<List<AccountResponse>>` | 查詢存款帳戶 | 給存錢目標頁選擇綁定帳戶。 | UC-13 |

## 5.4 Category API

| 模組 | Method | API | 傳入值 | 回傳值 | 用途 | 前端注意 | 對應 UC |
|---|---|---|---|---|---|---|---|
| Category | GET | `/api/categories` | Header；Query：`type=INCOME/EXPENSE`、`includeDisabled=true/false` | `ApiResponse<List<CategoryResponse>>` | 查詢分類總覽 | 分類管理頁可帶 `includeDisabled=true` 以顯示停用分類。 | UC-07 |
| Category | GET | `/api/categories/available` | Header；Query：`type=INCOME/EXPENSE` | `ApiResponse<List<CategoryResponse>>` | 查詢可用分類 | 交易表單使用，只顯示啟用分類。 | UC-08 |
| Category | GET | `/api/categories/{id}` | Header；Path：`id` | `ApiResponse<CategoryResponse>` | 查詢單筆分類 | 編輯分類前可載入。 | UC-07 |
| Category | POST | `/api/categories` | Header；Form Params：`categoryName`, `categoryType`, `icon`, `color` | `ApiResponse<CategoryResponse>` | 新增分類 | 不使用 request DTO；前端以 `postForm()` 送出。 | UC-07 |
| Category | PUT | `/api/categories/{id}` | Header；Path：`id`；Form Params：`categoryName`, `icon`, `color`, `isDisable` | `ApiResponse<CategoryResponse>` | 修改分類 / 停用分類 | 不使用 request DTO；id 放 path；停用可使用 `categoryApi.disable(id)`。 | UC-07 |

## 5.5 Transaction API

| 模組 | Method | API | 傳入值 | 回傳值 | 用途 | 前端注意 | 對應 UC |
|---|---|---|---|---|---|---|---|
| Transaction | GET | `/api/transactions/form-meta` | Header；Query：`transactionType=INCOME/EXPENSE` | `ApiResponse<Map>`：`accounts`, `categories` | 取得表單選項 | 目前後端回傳 Map，不使用 `TransactionFormMetaResponse`。 | UC-08、UC-10 |
| Transaction | POST | `/api/transactions` | Header；Body JSON：`{ transactionType, accountId, categoryId, transactionAmount, transactionDate, note }` | `ApiResponse<TransactionResponse>` | 新增收入 / 支出 | `transactionType` 由目前收入 / 支出模式帶入。 | UC-08 |
| Transaction | GET | `/api/transactions` | Header；Query：`startDate`, `endDate`, `accountId`, `categoryId`, `type`, `page`, `size` | `ApiResponse<Map>`：`summary`, `items`, `page`, `size`, `totalElements`, `totalPages`, `first`, `last` | 查詢交易列表與摘要 | 目前不使用 `TransactionListResponse`。 | UC-09 |
| Transaction | GET | `/api/transactions/{id}` | Header；Path：`id` | `ApiResponse<TransactionResponse>` | 查詢單筆交易 | id 由交易清單取得。 | UC-09、UC-10 |
| Transaction | PUT | `/api/transactions/{id}` | Header；Path：`id`；Body JSON：`{ transactionType, accountId, categoryId, transactionAmount, transactionDate, note }` | `ApiResponse<TransactionResponse>` | 修改交易 | id 放 path，body 不放 id。 | UC-10 |
| Transaction | DELETE | `/api/transactions/{id}` | Header；Path：`id` | `ApiResponse<TransactionResponse>` | 刪除交易 | 刪除後需刷新交易列表、月曆、摘要、每日獎勵與寵物狀態。 | UC-10 |
| Transaction | GET | `/api/transactions/summary` | Header；Query：`startDate`, `endDate`, `accountId`, `categoryId` | `ApiResponse<Map>`：`totalIncome`, `totalExpense`, `balance`, `transactionCount` | 查詢摘要 | 目前不使用 `TransactionSummaryResponse`。 | UC-09、UC-19 |

## 5.6 Transfer API

| 模組 | Method | API | 傳入值 | 回傳值 | 用途 | 前端注意 | 對應 UC |
|---|---|---|---|---|---|---|---|
| Transfer | POST | `/api/transfers` | Header；Body JSON：`{ fromAccountId, toAccountId, transactionAmount, transactionDate, note }` | `ApiResponse<TransferResponse>` | 新增轉帳 | 來源與目標不可相同。 | UC-11 |
| Transfer | GET | `/api/transfers` | Header | `ApiResponse<List<TransferResponse>>` | 查詢目前使用者轉帳列表 | 目前 Controller 未接查詢條件，前端若帶 params 後端暫不使用。 | UC-11 |
| Transfer | GET | `/api/transfers/{id}` | Header；Path：`id` | `ApiResponse<TransferResponse>` | 查詢單筆轉帳 | 依 `accountTransId` 查詢。 | UC-11 |
| Transfer | DELETE | `/api/transfers/{id}` | Header；Path：`id` | `ApiResponse<TransferAccountBalanceResponse>` | 刪除轉帳並回沖餘額 | 回傳受影響帳戶資訊。 | UC-11 |

## 5.7 Budget API

| 模組 | Method | API | 傳入值 | 回傳值 | 用途 | 前端注意 | 對應 UC |
|---|---|---|---|---|---|---|---|
| Budget | GET | `/api/budgets` | Header | `ApiResponse<List<BudgetResult>>` | 查詢所有預算與即時進度 | `BudgetResult` 內含 `Budget budget`, `currentSpent`, `progress`。 | UC-12 |
| Budget | POST | `/api/budgets` | Header；Body JSON：目前直接接 `Budget` Entity | `ApiResponse<Budget>` | 建立預算 | 已實作；目前仍直接接 Entity。 | UC-12 |
| Budget | GET | `/api/budgets/{id}` | Header；Path：`id` | `ApiResponse<Budget>` | 查詢單筆預算 | `budgetId` 為 String。 | UC-12 |
| Budget | PUT | `/api/budgets/{id}` | Header；Path：`id`；Body：`BigDecimal newAmount` | `ApiResponse<Budget>` | 修改預算金額 | 目前只更新金額。 | UC-12 |
| Budget | DELETE | `/api/budgets/{id}` | Header；Path：`id` | `ApiResponse<Void>` | 刪除預算 | 需檢查歸屬。 | UC-12 |

## 5.8 SavingGoal API

| 模組 | Method | API | 傳入值 | 回傳值 | 用途 | 前端注意 | 對應 UC |
|---|---|---|---|---|---|---|---|
| SavingGoal | POST | `/api/saving-goals` | Header；Body JSON：`{ goalName, targetAmount, startDate, endDate, accountId }` | `ApiResponse<SavingGoal>` | 建立存錢目標 | 使用 `SavingGoalRequest`。 | UC-13 |
| SavingGoal | POST | `/api/saving-goals/{id}/deposit` | Header；Path：`id`；Query：`amount`, `fromAccountId` | `ApiResponse<Void>` | 存入目標 | id 由前端目前選取目標暫存後放入 path。 | UC-13 |
| SavingGoal | PUT | `/api/saving-goals/{id}/complete` | Header；Path：`id` | `ApiResponse<SavingGoal>` | 完成目標 | 完成後更新狀態。 | UC-13 |
| SavingGoal | DELETE | `/api/saving-goals/{id}?toAccountId=1` | Header；Path：`id`；Query：`toAccountId` | `ApiResponse<Void>` | 刪除目標並指定金額轉回帳戶 | 前端 remove 目前已帶 `toAccountId`。 | UC-13 |
| SavingGoal | GET | `/api/saving-goals` | Header | `ApiResponse<List<SavingGoal>>` | 查詢所有目標 | 目前直接回傳 Entity。 | UC-13 |

## 5.9 Pet / DailyReward / PetEvent API

| 模組 | Method | API | 傳入值 | 回傳值 | 用途 | 前端注意 | 對應 UC |
|---|---|---|---|---|---|---|---|
| Pet | GET | `/api/pets/me` | Header | `ApiResponse<PetResponse>` | 查詢目前顯示寵物 | 回傳 `petId`, `petName`, `mood`, `cancan`, `modelId`, `riveName`, `isDisplayed`, `lastUpdateAt`。 | UC-14 |
| Pet | POST | `/api/pets/feed?foodType=CAN` | Header；Query：`foodType=CAN/FISH/SNACK/FEAST` | `ApiResponse<PetResponse>` | 餵食 | 不使用 body DTO；前端呼叫 `petApi.feed(foodType)`。 | UC-15 |
| Pet | POST | `/api/pets/login-tick` | Header；Query 可選：`loginDate=yyyy-MM-dd` | `ApiResponse<LoginTickResponse>` | 登入 streak | 正式環境不傳 loginDate；測試時可傳。 | UC-16 |
| DailyReward | POST | `/api/rewards/daily/calculate?date=yyyy-MM-dd` | Header；Query 可選：`date` | `ApiResponse<DailyRewardResponse>` | 手動重算每日記帳獎勵 | 正式交易流程會自動呼叫，這支主要供測試。 | UC-17 |
| DailyReward | GET | `/api/rewards/daily/today?date=yyyy-MM-dd` | Header；Query 可選：`date` | `ApiResponse<DailyRewardResponse>` | 查詢指定日獎勵 | 交易頁可用於比對新增交易前後 cancanDelta。 | UC-17 |
| DailyReward | GET | `/api/rewards/daily/history` | Header | `ApiResponse<List<DailyRewardResponse>>` | 查詢每日獎勵歷史 | 給寵物頁或獎勵歷史顯示。 | UC-17 |
| PetEvent | GET | `/api/pets/events?page=0&size=10` | Header；Query：`page`, `size` | `ApiResponse<Map>`：`items`, `page`, `size`, `totalElements`, `totalPages`, `first`, `last` | 查詢寵物事件 | `PetEventResponse` 不回傳 userId / petId。 | UC-18 |

## 5.10 Chart API

| 模組 | Method | API | 傳入值 | 回傳值 | 用途 | 前端注意 | 對應 UC |
|---|---|---|---|---|---|---|---|
| Chart | GET | `/api/charts/summary` | Query 可選：`startDate`, `endDate` | Map：`success`, `data.month_income`, `data.month_expense`, `data.net_amount`, `data.items` | 首頁 / 圖表摘要 | 部分實作；目前 Controller 使用 `default` 測試 userId，需改為目前登入者。 | UC-19 |
| Chart | GET | `/api/charts/expense-pie` | Query：`startDate`, `endDate`, `accountId` | 待定 | 支出分類圓餅圖 | 前端 api.js 已保留，後端尚未完整建立。 | UC-19 |
| Chart | GET | `/api/charts/cashflow-line` | Query：`startDate`, `endDate`, `groupBy` | 待定 | 收支折線圖 | 前端 api.js 已保留，後端尚未完整建立。 | UC-19 |
| Chart | GET | `/api/charts/monthly-cashflow-line` | Query：`year` | 待定 | 月收支折線 | 前端保留。 | UC-19 |
| Chart | GET | `/api/charts/daily-cashflow-line` | Query：`year`, `month` | 待定 | 日收支折線 | 前端保留。 | UC-19 |
| Chart | GET | `/api/charts/monthly-summary` | Query：`year`, `month` | 待定 | 月摘要 | 前端保留。 | UC-19 |
| Chart | GET | `/api/charts/daily-summary` | Query：`date` | 待定 | 日摘要 | 前端保留。 | UC-19 |

## 5.11 Admin API（保留規劃，待實作）

目前後端尚無 Admin Controller，`AdminAnalyticsService` 仍是空介面；僅有 `AdminConsumptionSummaryDto` 存在。因此本節作為後續架構評估與開發依據，不列為目前已完成 API。

| 模組 | Method | API | 傳入值 | 回傳值 | 用途 | 前端注意 | 對應 UC |
|---|---|---|---|---|---|---|---|
| Admin | GET | `/api/admin/users/consumption-summary` | Header：Admin token；Query：`year`, `month` | `ApiResponse<List<AdminConsumptionSummaryDto>>`：`userId`, `userName`, `totalExpense`, `totalIncome`, `avgDailyExpense`, `topCategoryName`, `transactionCount` | 管理員查看用戶消費總覽 | DTO 已有；Controller / Service 待實作。 | UC-20 |
| Admin | GET | `/api/admin/users/{userId}/analysis` | Header：Admin token；Path：`userId`；Query：`startDate`, `endDate` | 待新增 DTO：收入支出趨勢、支出分類、前五分類、帳戶使用、預算、目標、寵物 mood 趨勢 | 單一用戶詳細分析 | 待實作。 | UC-21 |
| Admin | GET | `/api/admin/group-analysis` | Header：Admin token；Query：`startDate`, `endDate` | 待新增 DTO：常見支出分類、平均月支出、常超支分類、低活躍用戶 | 群體分析 | 待實作。 | UC-22 |
| Admin | PUT | `/api/admin/pets/{petId}/mood` | Header：Admin token；Path：`petId`；Body JSON：`{ mood: 0~100 }` | `ApiResponse<PetResponse>` | 管理員測試寵物動畫狀態 | 待實作。 | UC-23 |

---

# 6. 附錄：建議、待確認與修正事項

## 6.1 目前需要修正 / 注意事項

| 編號 | 問題 | 影響 | 建議修正 |
|---|---|---|---|
| FIX-01 | `accounts.html` 新增帳戶送 `balance`，但 `AccountCreateRequest` 欄位是 `initialBalance`。 | 初始餘額可能無法正確進後端 DTO。 | 前端改成 `initialBalance: Number(document.getElementById('accBalance').value)`。 |
| FIX-02 | `api.js` 的 transfer list 註解寫「後端尚未實作」，但目前 `TransferController` 已有 `GET /api/transfers`。 | 文件與程式註解不一致。 | 更新註解：GET 已有，但目前不支援查詢條件。 |
| FIX-03 | `api.js` Pet 註解仍保留 legacy body：`userId`, `petId`, `feedType`。 | 容易誤導組員以為 feed 要傳 body。 | 改成目前實作：`POST /api/pets/feed?foodType=CAN`，不傳 userId / petId。 |
| FIX-04 | `ChartController` 使用 `transactionService.searchTransactions("default", ...)`。 | 圖表資料不會依目前登入者查詢。 | 改成由 token 取得目前登入者，並補 Authorization 驗證。 |
| FIX-05 | Budget / SavingGoal 目前部分 API 直接回傳 Entity。 | 前端可用，但長期容易暴露不必要關聯欄位。 | 專案期末可保留；後續版本再補 `BudgetResponse`、`SavingGoalResponse`。 |
| FIX-06 | Admin 功能文件已保留，但後端未完成。 | 不可在報告中說已實作。 | 文件標註「保留規劃 / 待實作」，並以 `AdminConsumptionSummaryDto` 為第一階段。 |

## 6.2 後續架構建議

| 類別 | 建議內容 |
|---|---|
| API 文件一致性 | 所有 API 詳細表格固定使用：`模組`、`Method`、`API`、`傳入值`、`回傳值`、`用途`、`前端注意`、`對應 UC`。 |
| DTO 演進 | 期末展示階段可保留已能運作的 Entity response；後續重構時優先補 Budget、SavingGoal、Chart、Admin 專用 response DTO。 |
| 管理員開發順序 | 第一階段可先完成 `GET /api/admin/users/consumption-summary`，因為目前已有 `AdminConsumptionSummaryDto`。第二階段再拆單一用戶分析與群體分析。 |
| 圖表 API | 建議先讓 ChartController 全部改為 token userId，再逐步補齊 expense-pie、cashflow-line、monthly-summary 等端點。 |
| 前端狀態管理 | 編輯用 id 應維持存在 state，不放入 request body；使用者歸屬一律交由後端 token 判斷。 |
