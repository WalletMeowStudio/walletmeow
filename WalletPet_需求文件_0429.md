# WalletPet 記帳遊戲需求文件修正版（SRS + SA + SD + API 對應版）

> 本版以「需求、系統分析、系統設計、API 對應、前端注意事項」為主，刪除過細的 DTO / Repository / Service 方法總表，避免文件過長且與實作版本不同步。  
> 本文件適合交給組員作為開發對照表：先看需求，再看流程，再看 API 與前端該注意的欄位。

---

# 1. 專案摘要

WalletPet 是一套結合「個人記帳」與「寵物互動」的理財系統。使用者可以管理帳戶、分類、收入、支出、轉帳、預算與存錢目標；系統會根據使用者的記帳、登入與餵食行為改變寵物狀態。

本專案的核心設計是：

1. 使用者透過收入 / 支出記錄掌握財務狀況。
2. 系統透過每日記帳獎勵給予 `cancan`（食物量）。
3. 使用者消耗 `cancan` 餵食寵物，使 `mood`（心情值）上升。
4. 系統透過登入 streak 鼓勵使用者持續使用。
5. 所有寵物狀態變化皆寫入 `pet_events`，供前端顯示事件紀錄與動畫依據。

---

# 2. SRS：需求規格說明書

## 2.1 系統目標

| 目標 | 說明 |
|---|---|
| 個人記帳 | 使用者可記錄收入、支出與轉帳，並查詢明細。 |
| 資產管理 | 使用者可建立與管理多個資產帳戶，交易與轉帳會同步影響帳戶餘額。 |
| 分類管理 | 使用者可使用系統預設分類，也可新增、編輯或停用自訂分類。 |
| 預算與目標 | 使用者可設定預算與存錢目標，追蹤理財進度。 |
| 圖表分析 | 使用者可透過圓餅圖、折線圖與摘要掌握收支結構。 |
| 寵物互動 | 系統以 `mood` 與 `cancan` 建立記帳回饋機制。 |
| 使用習慣養成 | 每日記帳獎勵與登入 streak 用於提高使用黏著度。 |
| 管理員分析 | 管理員可查看用戶收支、活躍度與消費行為分析。 |

## 2.2 系統角色

| 角色 | 權限說明 |
|---|---|
| 一般使用者 | 登入、管理帳戶、分類、交易、轉帳、預算、目標、查看圖表、查看與互動寵物。 |
| 管理員 | 查看用戶總覽、單一用戶分析、群體分析，並可進行寵物動畫測試與必要系統維護。 |
| 專案團隊 | 依分工完成前端頁面、後端 API、Service 邏輯與資料表整合。 |

## 2.3 功能需求（FR）

| 編號 | 功能 | 需求描述 | 主要資料表 | 主要 API / 模組 |
|---|---|---|---|---|
| FR-01 | 註冊 / 登入 | 使用者可註冊與登入，登入成功後取得 token。 | `users` | Auth / User |
| FR-02 | 建立初始資料 | 新使用者建立後，系統同步建立預設帳戶、預設分類與初始寵物。 | `accounts`, `categories`, `pets`, `pet_model` | User / InitialData |
| FR-03 | 帳戶管理 | 使用者可新增、查詢、修改與停用帳戶。 | `accounts` | Account |
| FR-04 | 分類管理 | 使用者可查詢、新增、編輯與停用收入 / 支出分類。 | `categories` | Category |
| FR-05 | 新增收入 / 支出 | 使用者可新增收入或支出，系統同步更新帳戶餘額。 | `transactions`, `accounts`, `categories` | Transaction |
| FR-06 | 交易查詢 | 使用者可依日期、帳戶、分類、型別查詢交易明細與摘要。 | `transactions`, `accounts`, `categories` | Transaction |
| FR-07 | 交易修改 / 刪除 | 修改時需還原舊交易影響再套用新資料；刪除時需回沖帳戶餘額。 | `transactions`, `accounts` | Transaction |
| FR-08 | 轉帳 | 使用者可於不同帳戶間轉帳，來源帳戶扣款、目標帳戶加款。 | `account_transactions`, `accounts` | Transfer |
| FR-09 | 預算 | 使用者可建立預算，系統依支出計算使用率與超支狀態。 | `budget`, `transactions`, `categories` | Budget |
| FR-10 | 存錢目標 | 使用者可建立目標，並透過帳戶轉入 / 提取管理進度。 | `saving_goals`, `accounts`, `account_transactions` | SavingGoal |
| FR-11 | 圖表分析 | 系統提供支出分類圓餅圖、收支折線圖與摘要。 | `transactions`, `categories`, `accounts` | Chart |
| FR-12 | 寵物狀態 | 使用者可查看寵物名稱、`mood`、`cancan`、模型與動畫狀態。 | `pets`, `pet_model` | Pet |
| FR-13 | 餵食 | 使用者消耗 `cancan` 餵食，增加 `mood`，並寫入事件。 | `pets`, `pet_events` | Pet / PetEvent |
| FR-14 | 每日記帳獎勵 | 新增收入 / 支出後，系統依當日交易筆數發放 `cancan`，每日最多 +5。 | `daily_record_rewards`, `pets`, `pet_events`, `transactions` | DailyReward |
| FR-15 | 登入 streak | 登入後寫入登入紀錄，計算連續登入與缺席天數，並調整 `mood`。 | `user_login_logs`, `pets`, `pet_events` | LoginStreak |
| FR-16 | 寵物事件紀錄 | 所有餵食、記帳獎勵、登入獎懲等事件皆記錄於 `pet_events`。 | `pet_events` | PetEvent |
| FR-17 | 管理員用戶分析 | 管理員可查看用戶收支、分類、預算、目標、活躍度與寵物狀態趨勢。 | 多表整合 | AdminAnalysis |
| FR-18 | 管理員動畫測試 | 管理員可測試不同 `mood` 區間對應的動畫狀態。 | `pets`, `pet_events` | AdminPetTest |

## 2.4 非功能需求（NFR）

| 編號 | 類別 | 需求內容 |
|---|---|---|
| NFR-01 | 可用性 | 新增交易流程應控制在 3 至 5 個主要步驟內。 |
| NFR-02 | 一致性 | API 回傳格式、欄位命名、前端顯示名稱需一致。 |
| NFR-03 | 安全性 | 除登入與註冊外，所有 API 皆需 token；Admin API 需檢查 `role = ADMIN`。 |
| NFR-04 | 資料正確性 | 交易、轉帳、預算、目標、寵物獎勵需保持資料一致。 |
| NFR-05 | 冪等性 | 每日記帳獎勵與每日登入紀錄不可重複發放。 |
| NFR-06 | 維護性 | Controller、Service、Repository 分層明確；不同模組責任不可混雜。 |
| NFR-07 | 擴充性 | 未來可擴充更多寵物模型、圖表、推播、發票掃描與會員機制。 |

## 2.5 重要業務規則

### 2.5.1 欄位說明

| 欄位 | 中文名稱 | 說明 |
|---|---|---|
| `mood` | 心情值 | 寵物目前情緒狀態，建議範圍 0 至 100，初始值 60。 |
| `cancan` | 食物量 | 使用者因記帳獎勵取得的餵食資源，餵食時會消耗。 |

### 2.5.2 記帳與帳戶規則

1. 每筆收入 / 支出必須對應一個帳戶與一個分類。
2. 收入會增加帳戶餘額；支出會減少帳戶餘額。
3. 修改交易時，需先還原舊交易對帳戶餘額的影響，再套用新交易。
4. 刪除交易時，需回沖帳戶餘額，並重新計算該日期每日記帳獎勵。
5. 存錢目標專用帳戶不應出現在一般收入 / 支出交易選項中。

### 2.5.3 餵食規則

| foodType | cancan 變化 | mood 變化 | 每日一般餵食 mood +3 上限 |
|---|---:|---:|---|
| CAN | -1 | +1 | 是 |
| FISH | -1 | +1 | 是 |
| SNACK | -1 | +1 | 是 |
| FEAST | -10 | +15 | 否 |

補充：

1. `CAN / FISH / SNACK` 合計每日最多使 `mood +3`。
2. `FEAST` 不受每日 +3 限制。
3. `cancan` 不足時不可餵食。
4. 餵食成功後要寫入 `pet_events`。

### 2.5.4 每日記帳獎勵規則

| 規則 | 說明 |
|---|---|
| 觸發時機 | 新增 / 修改 / 刪除收入或支出交易後，由 TransactionService 呼叫 DailyRewardService。 |
| 獎勵內容 | 當日每 1 筆收入 / 支出可使 `cancan +1`。 |
| 每日上限 | 每位使用者每日最多 `cancan +5`。 |
| 狀態紀錄 | 使用 `daily_record_rewards` 記錄當日交易筆數、是否達成與已發放 cancan。 |
| 事件紀錄 | 實際發放 cancan 時寫入 `pet_events`，eventType = `DAILY_BOOKKEEPING_REWARD`。 |
| 刪除交易 | 專題階段建議不回收已發 cancan，只重算 `transactionCount` 與 `qualified`。 |

### 2.5.5 登入 streak 規則

| 情境 | mood 變化 | eventType |
|---|---:|---|
| 今日已登入 | 0 | 不新增事件 |
| 連續登入 3 天 | +5 | `LOGIN_STREAK_3` |
| 連續登入 7 天 | +10 | `LOGIN_STREAK_7` |
| 缺席 3 天 | -10 | `LOGIN_ABSENCE_3` |
| 缺席 7 天以上 | -20 | `LOGIN_ABSENCE_7` |
| mood < 60 且連續登入 7 天 | 補到 60 | `MOOD_RECOVERY_60` |

補充：

1. `user_login_logs` 只記錄登入紀錄，不處理記帳獎勵。
2. `daily_record_rewards` 只處理每日記帳獎勵，不處理登入 streak。
3. 同一天重複呼叫 `/api/pets/login-tick` 不應重複新增紀錄或重複改變 mood。

---

# 3. SA：系統分析

## 3.1 主要使用情境（Use Case）

| UC | 名稱 | 主要流程 | 例外 / 替代流程 | 對應 FR | 前端主要頁面 |
|---|---|---|---|---|---|
| UC-01 | 註冊 / 登入 | 輸入帳密 → 驗證 → 回傳 token → 進入首頁。 | 帳密錯誤 → 顯示錯誤。 | FR-01 | login |
| UC-02 | 建立初始資料 | 註冊成功 → 建立預設帳戶、分類、寵物。 | 預設模型不存在 → 顯示系統錯誤。 | FR-02 | register |
| UC-03 | 帳戶管理 | 查詢帳戶 → 新增 / 修改 / 停用帳戶。 | 帳戶不屬於本人 → 禁止操作。 | FR-03 | accounts |
| UC-04 | 分類管理 | 查詢分類 → 新增 / 編輯 / 停用分類。 | 停用分類不可再用於新增交易。 | FR-04 | categories |
| UC-05 | 新增收入 / 支出 | 填表 → 新增交易 → 更新帳戶餘額 → 觸發每日記帳獎勵。 | 金額不合法、帳戶或分類停用 → 中止。 | FR-05, FR-14 | transactions |
| UC-06 | 編輯 / 刪除交易 | 讀取交易 → 修改或刪除 → 回沖舊帳戶影響 → 重算獎勵。 | 交易不存在或不屬於本人 → 中止。 | FR-07, FR-14 | transaction-detail |
| UC-07 | 查詢交易 | 篩選日期 / 帳戶 / 分類 / 型別 → 顯示清單與摘要。 | 無資料 → 顯示空狀態。 | FR-06 | transactions |
| UC-08 | 轉帳 | 選來源與目標帳戶 → 輸入金額 → 更新兩個帳戶餘額 → 寫入轉帳紀錄。 | 來源與目標相同、餘額不足 → 中止。 | FR-08 | transfers |
| UC-09 | 預算管理 | 建立預算 → 依支出計算使用率 → 顯示狀態。 | 日期錯誤或金額不合法 → 中止。 | FR-09 | budget |
| UC-10 | 存錢目標 | 建立目標 → 存入 / 提取 → 更新目標進度。 | 金額超過可用餘額 → 中止。 | FR-10 | goals |
| UC-11 | 圖表分析 | 選期間 → 查詢圖表資料 → 顯示圓餅圖與折線圖。 | 無資料 → 顯示空圖表。 | FR-11 | charts |
| UC-12 | 查看寵物 | 查詢 `/api/pets/me` → 顯示寵物狀態與動畫。 | 無寵物 → 顯示錯誤或補建初始寵物。 | FR-12 | dashboard / pet-widget |
| UC-13 | 餵食寵物 | 點選食物 → 扣 cancan → 增加 mood → 寫入事件。 | cancan 不足或已達上限 → 顯示提示。 | FR-13, FR-16 | pet-widget |
| UC-14 | 每日記帳獎勵 | 新增交易後自動計算 → 發放 cancan → 寫入事件。 | 當日已達 +5 → 不再發放。 | FR-14, FR-16 | transactions / pet-widget |
| UC-15 | 登入 streak | 登入後呼叫 login-tick → 寫入登入紀錄 → 計算 streak / missedDays → 更新 mood。 | 今日已登入 → 不重複發放。 | FR-15, FR-16 | login / dashboard |
| UC-16 | 查看寵物事件 | 查詢事件分頁 → 顯示事件紀錄。 | 無事件 → 空狀態。 | FR-16 | pet-events |
| UC-17 | 管理員分析 | 管理員查詢用戶總覽、單一用戶分析、群體分析。 | 權限不足 → 禁止存取。 | FR-17 | admin-* |
| UC-18 | 管理員動畫測試 | 管理員調整 mood → 回傳動畫狀態 → 寫入事件。 | mood 超出範圍 → 顯示錯誤。 | FR-18 | admin-pet-test |

## 3.2 核心流程分析

### 流程 A：登入與 login-tick

```text
使用者登入
→ AuthController 驗證帳密
→ 回傳 token
→ 前端呼叫 POST /api/pets/login-tick
→ LoginStreakService 檢查 user_login_logs
→ 今日未登入則新增登入紀錄
→ 計算 streakDays / missedDays
→ 必要時更新 pets.mood
→ 必要時寫入 pet_events
→ 回傳 LoginTickResponse
```

### 流程 B：新增收入 / 支出

```text
前端填寫交易表單
→ TransactionController 接收 request
→ TransactionService 驗證帳戶與分類屬於本人
→ 寫入 transactions
→ 更新 accounts.balance
→ 呼叫 DailyRewardService.handleDailyReward(currentUserId, transactionDate)
→ DailyRewardService 計算當日交易筆數
→ 必要時更新 daily_record_rewards / pets.cancan / pet_events
→ 回傳 TransactionResponse
```

### 流程 C：修改交易

```text
讀取原交易
→ 還原舊交易對舊帳戶餘額的影響
→ 驗證新帳戶與新分類
→ 套用新交易對新帳戶餘額的影響
→ 更新 transactions
→ 重算舊日期每日記帳獎勵
→ 若新日期不同，也重算新日期每日記帳獎勵
```

### 流程 D：餵食

```text
前端選擇 foodType
→ POST /api/pets/feed?foodType=CAN
→ PetService 查目前顯示寵物
→ 檢查 cancan 是否足夠
→ 若為一般餵食，檢查當日 mood +3 上限
→ 更新 pets.cancan / pets.mood
→ PetEventService.createEvent(...)
→ 回傳 PetResponse
```

## 3.3 資料流分析

| 資料流 | 說明 |
|---|---|
| 登入資料流 | `users` 驗證成功後，前端保存 token，後續 API 以 Header 帶入。 |
| 交易資料流 | `transactions` 是帳戶餘額、預算、圖表、每日記帳獎勵的主要來源。 |
| 轉帳資料流 | `account_transactions` 僅記錄帳戶間資金移動，不列入收入 / 支出統計。 |
| 每日記帳獎勵資料流 | `transactions` → `daily_record_rewards` → `pets.cancan` → `pet_events`。 |
| 登入 streak 資料流 | `user_login_logs` → `pets.mood` → `pet_events`。 |
| 餵食資料流 | `pets.cancan` 扣除 → `pets.mood` 增加 → `pet_events`。 |
| 圖表資料流 | 主要由 `transactions`、`categories`、`accounts` 彙整。 |

## 3.4 實體責任摘要

| 資料表 | 責任 | 不應負責 |
|---|---|---|
| `users` | 使用者身份與角色 | 不存業務統計結果 |
| `accounts` | 帳戶與餘額 | 不直接計算圖表 |
| `categories` | 收入 / 支出分類 | 不存交易金額 |
| `transactions` | 收入 / 支出 | 不存轉帳資料 |
| `account_transactions` | 帳戶轉帳 | 不列入收入 / 支出統計 |
| `budget` | 預算設定 | 不直接存死所有使用率，使用率可依交易即時計算 |
| `saving_goals` | 存錢目標 | 不處理一般交易統計 |
| `pets` | 寵物目前狀態 | 不存所有事件歷史 |
| `pet_events` | 寵物事件流水帳 | 不取代 `pets` 目前狀態 |
| `daily_record_rewards` | 每日記帳獎勵 | 不處理登入 streak |
| `user_login_logs` | 每日登入紀錄 | 不處理記帳獎勵 |
| `pet_model` | 寵物動畫模型資料 | 不處理寵物狀態 |

---

# 4. SD：系統設計

## 4.1 分層設計

| 層級 | 責任 | 注意事項 |
|---|---|---|
| Frontend | 表單、畫面、圖表、動畫、事件顯示 | 不傳 `userId` 作為資料歸屬判斷。 |
| Controller | 接收 API、處理參數、取得 currentUserId、呼叫 Service | 除測試用途外，不讓前端任意指定使用者。 |
| Service | 商業邏輯、跨表異動、交易一致性 | 需使用 `@Transactional` 保持資料一致。 |
| Repository | JPA 查詢與資料存取 | 避免把業務規則寫在 Repository。 |
| Database | 儲存實體資料與事件資料 | 唯一約束支援每日登入與每日獎勵冪等性。 |

## 4.2 後端模組責任

| 模組 | Controller / Service | 責任 |
|---|---|---|
| Auth | AuthController / AuthService | 登入、token 發放、角色判斷。 |
| User | UserController / UserService | 註冊、查詢目前使用者、建立初始資料。 |
| Account | AccountController / AccountService | 帳戶 CRUD、停用、帳戶選項。 |
| Category | CategoryController / CategoryService | 分類查詢、新增、編輯、停用。 |
| Transaction | TransactionController / TransactionService | 收入 / 支出 CRUD、帳戶餘額更新、觸發每日記帳獎勵。 |
| Transfer | TransferController / TransferService | 帳戶間轉帳與轉帳明細。 |
| Budget | BudgetController / BudgetService | 預算建立、查詢、使用率計算。 |
| SavingGoal | SavingGoalController / SavingGoalService | 存錢目標、存入、提取、完成。 |
| Chart | ChartController / ChartService | 收支圓餅圖、折線圖與摘要。 |
| Pet | PetController / PetService | 寵物查詢、餵食、調整 mood / cancan、建立預設寵物。 |
| PetEvent | PetEventController / PetEventService | 統一建立事件與查詢事件分頁。 |
| DailyReward | DailyRewardController / DailyRewardService | 每日記帳獎勵計算、查詢與重算。 |
| LoginStreak | LoginStreakService | 登入紀錄、連續登入、缺席天數與 mood 調整。 |
| AdminAnalysis | AdminAnalysisController / Service | 用戶總覽、單一用戶分析、群體分析。 |
| AdminPetTest | AdminPetTestController / Service | 管理員測試寵物 mood 與動畫狀態。 |

## 4.3 前端頁面與 API 對應

| 前端頁面 / 元件 | 主要 API | 前端注意事項 |
|---|---|---|
| login | `POST /api/auth/login` | 登入成功後保存 token；可接著呼叫 `/api/pets/login-tick`。 |
| dashboard | `/api/users/me`, `/api/pets/me`, `/api/transactions/summary`, `/api/pets/events` | 首頁應整合使用者摘要、寵物狀態、近期交易與事件。 |
| pet-widget | `GET /api/pets/me`, `POST /api/pets/feed`, `POST /api/pets/login-tick` | 餵食只傳 `foodType`；不要傳 `userId` 或 `petId`。 |
| accounts | `GET /api/accounts`, `POST /api/accounts`, `PUT /api/accounts/{id}`, `DELETE /api/accounts/{id}` | 停用帳戶後不應出現在新增交易選項。 |
| categories | `GET /api/categories`, `POST /api/categories`, `PUT /api/categories/{id}` | 新增 / 編輯分類使用表單欄位；停用後不可選用。 |
| transactions | `GET /api/transactions/form-meta`, `POST /api/transactions`, `GET /api/transactions`, `PUT /api/transactions/{id}`, `DELETE /api/transactions/{id}` | 新增交易後需重新載入帳戶餘額、交易清單、DailyReward、寵物狀態。 |
| transfers | `POST /api/transfers`, `GET /api/transfers` | 來源與目標帳戶不可相同。 |
| budget | `GET /api/budgets`, `POST /api/budgets`, `PUT /api/budgets/{id}`, `DELETE /api/budgets/{id}` | 使用率由支出交易計算，非單純讀欄位。 |
| goals | `GET /api/saving-goals`, `POST /api/saving-goals`, `POST /api/saving-goals/{id}/deposit`, `POST /api/saving-goals/{id}/withdraw` | 存入 / 提取本質上會影響帳戶餘額。 |
| charts | `/api/charts/*` | 無資料時應顯示空圖，不顯示錯誤畫面。 |
| pet-events | `GET /api/pets/events?page=0&size=10` | `PetEventResponse` 不回傳 `userId`、`petId`。 |
| admin-pages | `/api/admin/*` | 必須使用 admin token，權限不足時導回或顯示錯誤。 |

## 4.4 跨 Service 交易一致性

| 情境 | 必須同步完成 |
|---|---|
| 新增收入 / 支出 | 寫入 `transactions` → 更新 `accounts.balance` → 更新 `daily_record_rewards` → 必要時更新 `pets.cancan` → 寫入 `pet_events`。 |
| 修改收入 / 支出 | 還原舊帳戶餘額 → 套用新帳戶餘額 → 更新交易 → 重算舊日期 / 新日期獎勵。 |
| 刪除收入 / 支出 | 還原帳戶餘額 → 刪除交易 → 重算該日期每日記帳獎勵。 |
| 新增轉帳 | 來源帳戶扣款 → 目標帳戶加款 → 寫入 `account_transactions`。 |
| 餵食 | 檢查 cancan → 更新 `pets.cancan` / `pets.mood` → 寫入 `pet_events`。 |
| 登入 tick | 寫入 `user_login_logs` → 計算 streak / missedDays → 更新 `pets.mood` → 寫入 `pet_events`。 |

## 4.5 事件類型設計

| eventType | 觸發來源 | moodDelta | cancanDelta | 說明 |
|---|---|---:|---:|---|
| `PET_FEED_CAN` | 餵食 CAN | +1 或 0 | -1 | 一般餵食，受每日 +3 上限。 |
| `PET_FEED_FISH` | 餵食 FISH | +1 或 0 | -1 | 一般餵食，受每日 +3 上限。 |
| `PET_FEED_SNACK` | 餵食 SNACK | +1 或 0 | -1 | 一般餵食，受每日 +3 上限。 |
| `PET_FEED_FEAST` | 餵食 FEAST | +15 | -10 | 大餐，不受每日 +3 上限。 |
| `DAILY_BOOKKEEPING_REWARD` | 每日記帳獎勵 | 0 | +1 | 每日最多 +5。 |
| `LOGIN_STREAK_3` | 連續登入 3 天 | +5 | 0 | 登入獎勵。 |
| `LOGIN_STREAK_7` | 連續登入 7 天 | +10 | 0 | 登入獎勵。 |
| `LOGIN_ABSENCE_3` | 缺席 3 天 | -10 | 0 | 登入懲罰。 |
| `LOGIN_ABSENCE_7` | 缺席 7 天以上 | -20 | 0 | 登入懲罰。 |
| `MOOD_RECOVERY_60` | 連 7 天且 mood < 60 | 補到 60 | 0 | mood 回補。 |
| `ADMIN_MOOD_ADJUST` | 管理員動畫測試 | 依輸入 | 0 | 測試用。 |

---

# 5. API 規格與對應關係

## 5.1 共用規則

| 項目 | 規格 |
|---|---|
| Base URL | 本機測試需加 context-path：`http://localhost:8080/walletpet` |
| Header | 除登入 / 註冊外，皆需 `Authorization: Bearer {token}` |
| 使用者歸屬 | 後端由 token 取得 `currentUserId`；前端不傳 `userId` 作為資料歸屬依據。 |
| 回傳格式 | `{ "success": true, "message": "查詢成功", "data": ... }` |
| 分頁格式 | `{ items, page, size, totalElements, totalPages, first, last }` |
| 日期格式 | `yyyy-MM-dd` |

## 5.2 API 總表

| 模組 | Method | API | 傳入 | 回傳重點 | 對應 UC | 前端注意 |
|---|---|---|---|---|---|---|
| Auth | POST | `/api/auth/login` | Body：`userName`, `password` | token、user 資訊 | UC-01 | 登入成功後保存 token。 |
| User | POST | `/api/users/register` | Body：`userName`, `password`, `petName` | 建立使用者 | UC-02 | 註冊後應可立即登入或導回登入頁。 |
| User | GET | `/api/users/me` | Header token | 目前使用者 | UC-01 | 不需傳 userId。 |
| Account | GET | `/api/accounts` | Query：`includeDisabled` | 帳戶列表 | UC-03 | 新增交易時只顯示可用帳戶。 |
| Account | POST | `/api/accounts` | Body：帳戶資料 | 新增帳戶 | UC-03 | 初始餘額需為數字。 |
| Account | PUT | `/api/accounts/{id}` | Body：可修改欄位 | 修改帳戶 | UC-03 | id 放 path。 |
| Account | DELETE | `/api/accounts/{id}` | Path：id | 停用帳戶 | UC-03 | 前端文字建議顯示「停用」。 |
| Category | GET | `/api/categories` | Query：`type`, `includeDisabled` | 分類列表 | UC-04 | 分類頁使用。 |
| Category | GET | `/api/categories/available` | Query：`type` | 可選用分類 | UC-05 | 新增交易表單使用，只回啟用分類。 |
| Category | POST | `/api/categories` | Form：`categoryName`, `categoryType`, `icon`, `color` | 新增分類 | UC-04 | 目前採 RequestParam / form-urlencoded。 |
| Category | GET | `/api/categories/{id}` | Path：id | 單筆分類 | UC-04 | 編輯表單載入。 |
| Category | PUT | `/api/categories/{id}` | Form：`categoryName`, `icon`, `color`, `isDisable` | 更新分類 | UC-04 | 不傳 userId。 |
| Transaction | GET | `/api/transactions/form-meta` | Query：`transactionType` | `accounts`, `categories` | UC-05 | 新增 / 編輯交易前先呼叫。 |
| Transaction | POST | `/api/transactions` | JSON：交易資料 | `TransactionResponse` | UC-05, UC-14 | 成功後重載帳戶、交易、寵物。 |
| Transaction | GET | `/api/transactions` | Query：`startDate`, `endDate`, `accountId`, `categoryId`, `type`, `page`, `size` | summary + items | UC-07 | summary 統計全部篩選結果，不只當頁。 |
| Transaction | GET | `/api/transactions/{id}` | Path：id | 單筆交易 | UC-06 | 編輯明細用。 |
| Transaction | PUT | `/api/transactions/{id}` | JSON：交易資料 | 更新後交易 | UC-06 | 日期改變會重算舊 / 新日期獎勵。 |
| Transaction | DELETE | `/api/transactions/{id}` | Path：id | 刪除前交易 | UC-06 | 成功後重載帳戶與每日獎勵。 |
| Transaction | GET | `/api/transactions/summary` | Query：日期、帳戶、分類 | 收支摘要 | UC-07, UC-11 | 圖表與首頁可共用。 |
| Transfer | POST | `/api/transfers` | JSON：fromAccountId, toAccountId, amount, date | 轉帳結果 | UC-08 | 來源與目標不可相同。 |
| Transfer | GET | `/api/transfers` | Query：日期、來源、目標、分頁 | 轉帳列表 | UC-08 | 轉帳不列入收入 / 支出。 |
| Budget | GET | `/api/budgets` | Query：status | 預算列表與使用率 | UC-09 | 使用率由支出即時計算。 |
| Budget | POST | `/api/budgets` | JSON：預算資料 | 新增預算 | UC-09 | category 預算需帶 categoryId。 |
| Budget | PUT | `/api/budgets/{id}` | JSON：可修改欄位 | 更新預算 | UC-09 | 修改後重算 usage。 |
| Budget | DELETE | `/api/budgets/{id}` | Path：id | 刪除預算 | UC-09 | 不刪交易。 |
| SavingGoal | GET | `/api/saving-goals` | Query：status | 目標列表 | UC-10 | 顯示進度條。 |
| SavingGoal | POST | `/api/saving-goals` | JSON：目標資料 | 新增目標 | UC-10 | 建議選 `isSavingAccount=true` 帳戶。 |
| SavingGoal | POST | `/api/saving-goals/{id}/deposit` | JSON：fromAccountId, amount | 存入結果 | UC-10 | 本質是轉帳。 |
| SavingGoal | POST | `/api/saving-goals/{id}/withdraw` | JSON：toAccountId, amount | 提取結果 | UC-10 | 不可超過目標帳戶餘額。 |
| Chart | GET | `/api/charts/expense-pie` | Query：startDate, endDate, accountId | 分類占比 | UC-11 | 無資料顯示空圖。 |
| Chart | GET | `/api/charts/cashflow-line` | Query：startDate, endDate, groupBy | 收支趨勢 | UC-11 | groupBy 可為 DAY / MONTH。 |
| Pet | GET | `/api/pets/me` | Header token | PetResponse | UC-12 | 首頁 pet-widget 使用。 |
| Pet | POST | `/api/pets/feed` | Query：`foodType=CAN/FISH/SNACK/FEAST` | PetResponse | UC-13 | 不傳 petId / userId。 |
| Pet | POST | `/api/pets/login-tick` | Query 可選：`loginDate` | LoginTickResponse | UC-15 | 正式環境不傳 date；測試 streak 可傳。 |
| PetEvent | GET | `/api/pets/events` | Query：page, size | 事件分頁 | UC-16 | 不回 userId / petId。 |
| DailyReward | POST | `/api/rewards/daily/calculate` | Query：date | DailyRewardResponse | UC-14 | 測試或手動重算用。 |
| DailyReward | GET | `/api/rewards/daily/today` | Query 可選：date | DailyRewardResponse | UC-14 | 顯示今日記帳獎勵狀態。 |
| DailyReward | GET | `/api/rewards/daily/history` | Header token | 歷史列表 | UC-14 | 可做獎勵紀錄頁。 |
| AdminAnalysis | GET | `/api/admin/users/overview` | Query：year, month, page, size | 用戶總覽 | UC-17 | 需 admin token。 |
| AdminAnalysis | GET | `/api/admin/users/{userId}/analysis` | Path userId + Query 日期 | 單一用戶分析 | UC-17 | 需 admin token。 |
| AdminAnalysis | GET | `/api/admin/analytics/group` | Query 日期 | 群體分析 | UC-17 | 需 admin token。 |
| AdminPetTest | PUT | `/api/admin/pets/{petId}/mood` | JSON：mood, reason | 測試結果 | UC-18 | 需 admin token。 |

## 5.3 API 資料回傳格式表

> 本表用於補足前端串接時最常需要確認的 `data` 結構。所有 API 外層皆使用共用格式：`{ success, message, data, errorCode }`。表中的「data 回傳格式」只描述 `data` 內容。

| 模組 | Method | API | data 回傳格式 | 前端主要用途 |
|---|---|---|---|---|
| Auth | POST | `/api/auth/login` | `{ userId, userName, role, token }` | 儲存 token、角色判斷、導頁。 |
| User | POST | `/api/users/register` | `{ userId, userName, role, createdAt }` | 註冊成功提示，導回登入或自動登入。 |
| User | GET | `/api/users/me` | `{ userId, userName, role, createdAt }` | 顯示目前使用者與權限。 |
| Account | GET | `/api/accounts` | `[ { accountId, accountName, balance, isLiability, isSavingAccount, isDisabled, createdAt } ]` | 帳戶列表、交易表單帳戶選項。 |
| Account | POST | `/api/accounts` | `{ accountId, accountName, balance, isLiability, isSavingAccount, isDisabled, createdAt }` | 新增成功後更新帳戶列表。 |
| Account | PUT | `/api/accounts/{id}` | `{ accountId, accountName, balance, isLiability, isSavingAccount, isDisabled, updatedAt }` | 編輯成功後更新畫面。 |
| Account | DELETE | `/api/accounts/{id}` | `{ accountId, isDisabled }` | 停用後從可選帳戶移除。 |
| Category | GET | `/api/categories` | `[ { categoryId, categoryName, categoryType, icon, color, isSystem, isDisable, createdAt } ]` | 分類管理頁列表。 |
| Category | GET | `/api/categories/available` | `[ { categoryId, categoryName, categoryType, icon, color } ]` | 新增 / 編輯交易表單下拉選單。 |
| Category | POST | `/api/categories` | `{ categoryId, categoryName, categoryType, icon, color, isSystem, isDisable, createdAt }` | 新增分類後更新列表。 |
| Category | GET | `/api/categories/{id}` | `{ categoryId, categoryName, categoryType, icon, color, isSystem, isDisable, createdAt }` | 編輯分類前載入資料。 |
| Category | PUT | `/api/categories/{id}` | `{ categoryId, categoryName, categoryType, icon, color, isSystem, isDisable, updatedAt }` | 編輯或停用分類後更新列表。 |
| Transaction | GET | `/api/transactions/form-meta` | `{ accounts: [ { accountId, accountName, balance, isSavingAccount } ], categories: [ { categoryId, categoryName, categoryType, icon, color } ] }` | 新增 / 編輯交易前載入帳戶與分類。 |
| Transaction | POST | `/api/transactions` | `{ transactionId, transactionType, accountId, accountName, categoryId, categoryName, transactionAmount, transactionDate, note, createdAt }` | 新增交易後更新交易列表、帳戶、DailyReward、Pet。 |
| Transaction | GET | `/api/transactions` | `{ summary: { totalIncome, totalExpense, balance, transactionCount }, items: [TransactionResponse], page, size, totalElements, totalPages, first, last }` | 交易清單與分頁。 |
| Transaction | GET | `/api/transactions/{id}` | `{ transactionId, transactionType, accountId, accountName, categoryId, categoryName, transactionAmount, transactionDate, note, createdAt }` | 單筆明細與編輯載入。 |
| Transaction | PUT | `/api/transactions/{id}` | `{ transactionId, transactionType, accountId, accountName, categoryId, categoryName, transactionAmount, transactionDate, note, createdAt }` | 修改後更新明細、帳戶與獎勵狀態。 |
| Transaction | DELETE | `/api/transactions/{id}` | `{ transactionId, transactionType, accountId, accountName, categoryId, categoryName, transactionAmount, transactionDate, note }` | 刪除後可顯示被刪除項目並刷新列表。 |
| Transaction | GET | `/api/transactions/summary` | `{ totalIncome, totalExpense, balance, transactionCount }` | 首頁、圖表、預算參考。 |
| Transfer | POST | `/api/transfers` | `{ accountTransId, fromAccountId, fromAccountName, fromAccountBalance, toAccountId, toAccountName, toAccountBalance, transactionAmount, transactionDate, note, createdAt }` | 轉帳成功後更新兩個帳戶餘額。 |
| Transfer | GET | `/api/transfers` | `{ summary: { transferCount, totalTransferAmount }, items: [TransferResponse], page, size, totalElements, totalPages, first, last }` | 轉帳明細分頁。 |
| Budget | GET | `/api/budgets` | `[ { budgetId, budgetName, budgetScope, categoryId, categoryName, budgetAmount, spentAmount, remainingAmount, usageRate, status, startDate, endDate } ]` | 預算列表與進度條。 |
| Budget | POST | `/api/budgets` | `{ budgetId, budgetName, budgetScope, categoryId, budgetAmount, startDate, endDate, status }` | 建立預算後更新列表。 |
| Budget | PUT | `/api/budgets/{id}` | `{ budgetId, budgetName, budgetAmount, spentAmount, remainingAmount, usageRate, status }` | 修改後更新使用率。 |
| Budget | DELETE | `/api/budgets/{id}` | `{ budgetId }` | 刪除後更新列表。 |
| SavingGoal | GET | `/api/saving-goals` | `[ { savingGoalId, goalName, targetAmount, currentAmount, progressRate, accountId, accountName, endDate, status } ]` | 存錢目標列表。 |
| SavingGoal | POST | `/api/saving-goals` | `{ savingGoalId, goalName, targetAmount, currentAmount, progressRate, accountId, status, createdAt }` | 建立目標後更新列表。 |
| SavingGoal | POST | `/api/saving-goals/{id}/deposit` | `{ savingGoalId, currentAmount, progressRate, fromAccountBalance, goalAccountBalance }` | 存入後更新目標進度與帳戶餘額。 |
| SavingGoal | POST | `/api/saving-goals/{id}/withdraw` | `{ savingGoalId, currentAmount, progressRate, toAccountBalance, goalAccountBalance }` | 提取後更新目標進度與帳戶餘額。 |
| Chart | GET | `/api/charts/expense-pie` | `[ { categoryId, categoryName, amount, percentage, color } ]` | 圓餅圖資料。 |
| Chart | GET | `/api/charts/cashflow-line` | `[ { label, income, expense, balance } ]` | 折線圖資料。 |
| Pet | GET | `/api/pets/me` | `{ petId, petName, mood, cancan, modelId, riveName, isDisplayed, lastUpdateAt }` | 寵物元件與動畫狀態。 |
| Pet | POST | `/api/pets/feed` | `{ petId, petName, mood, cancan, modelId, riveName, isDisplayed, lastUpdateAt }` | 餵食後更新寵物狀態。 |
| Pet | POST | `/api/pets/login-tick` | `{ loginDate, alreadyLoggedToday, loginStreakDays, missedDays, moodDelta, moodRecoveredTo60, eventType, pet, createdAt }` | 登入 streak 顯示與動畫提示。 |
| PetEvent | GET | `/api/pets/events` | `{ items: [ { petEventId, petName, eventType, moodDelta, cancanDelta, reward, createdAt } ], page, size, totalElements, totalPages, first, last }` | 寵物事件列表；不含 `userId`、`petId`。 |
| DailyReward | POST | `/api/rewards/daily/calculate` | `{ dailyRewardId, rewardDate, qualified, transactionCount, streakDays, rewardType, rewardValue, moodDelta, cancanDelta, claimedAt, createdAt, updatedAt }` | 測試 / 手動重算每日記帳獎勵。 |
| DailyReward | GET | `/api/rewards/daily/today` | `{ dailyRewardId, rewardDate, qualified, transactionCount, streakDays, rewardType, rewardValue, moodDelta, cancanDelta, claimedAt, createdAt, updatedAt }` | 顯示今日或指定日期獎勵狀態。 |
| DailyReward | GET | `/api/rewards/daily/history` | `[ DailyRewardResponse ]` | 每日記帳獎勵歷史。 |
| AdminAnalysis | GET | `/api/admin/users/overview` | `{ items: [ { userId, userName, monthlyExpense, monthlyIncome, transactionCount, averageDailyExpense, topExpenseCategory, oftenOverBudget, bookkeepingStreakDays } ], page, size, totalElements, totalPages }` | 管理員用戶總覽。 |
| AdminAnalysis | GET | `/api/admin/users/{userId}/analysis` | `{ userId, userName, cashflowLine, expensePie, topFiveExpenseCategories, accountUsageRatio, bookkeepingActiveCalendar, budgetRecords, savingGoals, petMoodTrend }` | 單一用戶詳細分析。 |
| AdminAnalysis | GET | `/api/admin/analytics/group` | `{ commonExpenseCategories, averageMonthlyExpense, overBudgetCategories, lowestActivityUsers }` | 群體分析。 |
| AdminPetTest | PUT | `/api/admin/pets/{petId}/mood` | `{ petId, petName, mood, animation, eventType, updatedAt }` | 管理員測試動畫狀態。 |

## 5.4 UC–FR–API 對應矩陣

> 本表用於驗收與分工確認。若某 UC 測試失敗，可依本表回查對應 FR 與 API。

| UC | 使用情境 | 對應 FR | 主要 API | 驗收重點 |
|---|---|---|---|---|
| UC-01 | 註冊 / 登入 | FR-01 | `POST /api/auth/login`, `GET /api/users/me` | 登入成功取得 token；後續 API 可用 token 存取。 |
| UC-02 | 建立初始資料 | FR-02 | `POST /api/users/register`, `GET /api/accounts`, `GET /api/categories`, `GET /api/pets/me` | 註冊後有預設帳戶、分類與寵物。 |
| UC-03 | 帳戶管理 | FR-03 | `GET /api/accounts`, `POST /api/accounts`, `PUT /api/accounts/{id}`, `DELETE /api/accounts/{id}` | 可新增、修改、停用；停用帳戶不再作為交易選項。 |
| UC-04 | 分類管理 | FR-04 | `GET /api/categories`, `POST /api/categories`, `GET /api/categories/{id}`, `PUT /api/categories/{id}` | 可新增 / 編輯 / 停用分類；停用分類不出現在 available 清單。 |
| UC-05 | 新增收入 / 支出 | FR-05, FR-14, FR-16 | `GET /api/transactions/form-meta`, `POST /api/transactions`, `GET /api/rewards/daily/today`, `GET /api/pets/me`, `GET /api/pets/events` | 新增交易後帳戶餘額更新，可能發放 cancan 並新增事件。 |
| UC-06 | 編輯 / 刪除交易 | FR-07, FR-14 | `GET /api/transactions/{id}`, `PUT /api/transactions/{id}`, `DELETE /api/transactions/{id}` | 修改需還原舊影響再套用新影響；刪除需回沖餘額與重算 DailyReward。 |
| UC-07 | 查詢交易 | FR-06 | `GET /api/transactions`, `GET /api/transactions/summary` | 條件查詢與 summary 正確；分頁資料正確。 |
| UC-08 | 轉帳 | FR-08 | `POST /api/transfers`, `GET /api/transfers` | 來源帳戶扣款、目標帳戶加款；不列入收入 / 支出統計。 |
| UC-09 | 預算管理 | FR-09 | `GET /api/budgets`, `POST /api/budgets`, `PUT /api/budgets/{id}`, `DELETE /api/budgets/{id}` | 預算使用率依支出交易計算；刪預算不刪交易。 |
| UC-10 | 存錢目標 | FR-10 | `GET /api/saving-goals`, `POST /api/saving-goals`, `POST /api/saving-goals/{id}/deposit`, `POST /api/saving-goals/{id}/withdraw` | 存入 / 提取後帳戶餘額與目標進度同步。 |
| UC-11 | 圖表分析 | FR-11 | `GET /api/charts/expense-pie`, `GET /api/charts/cashflow-line`, `GET /api/transactions/summary` | 無資料顯示空圖；金額與交易摘要一致。 |
| UC-12 | 查看寵物 | FR-12 | `GET /api/pets/me` | 回傳 mood、cancan、modelId、riveName；前端可決定動畫。 |
| UC-13 | 餵食寵物 | FR-13, FR-16 | `POST /api/pets/feed`, `GET /api/pets/events` | cancan 扣除、mood 增加、一般餵食每日 +3 上限、事件新增。 |
| UC-14 | 每日記帳獎勵 | FR-14, FR-16 | `POST /api/transactions`, `GET /api/rewards/daily/today`, `GET /api/rewards/daily/history`, `POST /api/rewards/daily/calculate` | 同日 cancan 最多 +5；第 6 筆不再發放；事件紀錄正確。 |
| UC-15 | 登入 streak | FR-15, FR-16 | `POST /api/pets/login-tick`, `GET /api/pets/me`, `GET /api/pets/events` | 同日不重複；連 3 / 7 天與缺席 3 / 7 天 mood 變化正確。 |
| UC-16 | 查看寵物事件 | FR-16 | `GET /api/pets/events?page=0&size=10` | 分頁正確；不回傳 userId / petId。 |
| UC-17 | 管理員分析 | FR-17 | `GET /api/admin/users/overview`, `GET /api/admin/users/{userId}/analysis`, `GET /api/admin/analytics/group` | 需 admin token；回傳用戶與群體分析資料。 |
| UC-18 | 管理員動畫測試 | FR-18 | `PUT /api/admin/pets/{petId}/mood` | mood 限制 0–100；更新寵物狀態並寫入 `ADMIN_MOOD_ADJUST`。 |
## 5.5 主要 API Request / Response 範例

### 5.3.1 新增交易

```http
POST /walletpet/api/transactions
Authorization: Bearer {token}
Content-Type: application/json
```

```json
{
  "transactionType": "EXPENSE",
  "accountId": 1,
  "categoryId": "CAT202604270001",
  "transactionAmount": 120,
  "transactionDate": "2026-04-29",
  "note": "午餐"
}
```

成功後，後端需完成：

```text
transactions 新增一筆
accounts.balance 扣款或加款
daily_record_rewards 新增或更新
pets.cancan 可能 +1
pet_events 可能新增 DAILY_BOOKKEEPING_REWARD
```

### 5.3.2 查詢交易列表

```http
GET /walletpet/api/transactions?startDate=2026-04-01&endDate=2026-04-30&page=0&size=10
Authorization: Bearer {token}
```

```json
{
  "success": true,
  "message": "查詢成功",
  "data": {
    "summary": {
      "totalIncome": 30000,
      "totalExpense": 12000,
      "balance": 18000,
      "transactionCount": 20
    },
    "items": [
      {
        "transactionId": "TXN202604270001",
        "transactionType": "EXPENSE",
        "accountId": 1,
        "accountName": "現金",
        "categoryId": "CAT202604270001",
        "categoryName": "餐飲",
        "transactionAmount": 120,
        "transactionDate": "2026-04-29",
        "note": "午餐"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 20,
    "totalPages": 2,
    "first": true,
    "last": false
  }
}
```

### 5.3.3 餵食

```http
POST /walletpet/api/pets/feed?foodType=CAN
Authorization: Bearer {token}
```

```json
{
  "success": true,
  "message": "餵食成功",
  "data": {
    "petId": "PET202604270001",
    "petName": "小咪",
    "mood": 81,
    "cancan": 4,
    "modelId": 1,
    "riveName": "cat.riv",
    "isDisplayed": true,
    "lastUpdateAt": "2026-04-29T10:00:00"
  }
}
```

### 5.3.4 登入 tick

```http
POST /walletpet/api/pets/login-tick
Authorization: Bearer {token}
```

```json
{
  "success": true,
  "message": "登入紀錄完成",
  "data": {
    "loginDate": "2026-04-29",
    "alreadyLoggedToday": false,
    "loginStreakDays": 3,
    "missedDays": 0,
    "moodDelta": 5,
    "moodRecoveredTo60": false,
    "eventType": "LOGIN_STREAK_3",
    "pet": {
      "petId": "PET202604270001",
      "petName": "小咪",
      "mood": 85,
      "cancan": 4
    },
    "createdAt": "2026-04-29T10:00:00"
  }
}
```

### 5.3.5 查詢每日記帳獎勵

```http
GET /walletpet/api/rewards/daily/today?date=2026-04-29
Authorization: Bearer {token}
```

```json
{
  "success": true,
  "message": "查詢成功",
  "data": {
    "dailyRewardId": 1,
    "rewardDate": "2026-04-29",
    "qualified": true,
    "transactionCount": 1,
    "streakDays": 1,
    "rewardType": "CAT_FOOD",
    "rewardValue": 1,
    "moodDelta": 0,
    "cancanDelta": 1,
    "claimedAt": "2026-04-29T10:00:00"
  }
}
```

---

# 6. 前端整合注意事項

## 6.1 Header 與登入狀態

| 項目 | 前端處理 |
|---|---|
| token | 登入成功後保存，後續 API 皆帶 `Authorization: Bearer {token}`。 |
| userId | 一般頁面不要傳 `userId` 給後端判斷資料歸屬。 |
| 401 / 403 | 401 導回登入；403 顯示權限不足。 |

## 6.2 表單與 API 傳值

| 功能 | 前端傳值方式 | 注意事項 |
|---|---|---|
| Category 新增 / 修改 | `x-www-form-urlencoded` 或 Query/Form params | 因後端採 `@RequestParam`，不是 JSON body。 |
| Transaction 新增 / 修改 | JSON body | 保留 `TransactionCreateRequest` / `TransactionUpdateRequest`。 |
| Pet Feed | Query：`foodType` | 不傳 `petId`、不傳 `userId`。 |
| Login Tick | Query 可選：`loginDate` | 正式環境不傳；測試 streak 時可傳。 |
| DailyReward 查詢 | Query：`date` 可選 | 正式發獎由交易 API 自動觸發。 |

## 6.3 新增交易後前端應更新的資料

新增、修改或刪除交易成功後，建議前端重新載入：

1. 交易列表：`GET /api/transactions`
2. 帳戶列表或帳戶摘要：`GET /api/accounts`
3. 每日記帳獎勵狀態：`GET /api/rewards/daily/today?date=交易日期`
4. 寵物狀態：`GET /api/pets/me`
5. 寵物事件：`GET /api/pets/events?page=0&size=10`
6. 圖表或預算資料：依頁面需求重新查詢

## 6.4 寵物動畫對應建議

| mood 範圍 | 建議動畫狀態 |
|---|---|
| 0 - 39 | sad |
| 40 - 59 | low |
| 60 - 79 | normal |
| 80 - 100 | happy |

前端可依 `PetResponse.mood` 決定基本動畫，再依最近一筆 `pet_events.eventType` 觸發短暫互動動畫。

## 6.5 空狀態與錯誤提示

| 情境 | 前端提示 |
|---|---|
| 無帳戶 | 顯示「尚未建立帳戶」。 |
| 無分類 | 顯示「尚未建立分類」。 |
| 無交易 | 顯示「目前沒有交易紀錄」。 |
| cancan 不足 | 顯示「食物量不足，請先記帳取得 cancan」。 |
| 一般餵食 mood 今日已達 +3 | 顯示「今日一般餵食心情值已達上限」。 |
| 今日已 login-tick | 不顯示錯誤，可顯示「今日已簽到」。 |
| DailyReward 已達 +5 | 不顯示錯誤，可顯示「今日記帳獎勵已達上限」。 |

---

# 7. 分工對應

| 分組 | 前端責任 | 後端責任 | 主要 API | 主要資料表 |
|---|---|---|---|---|
| 登入與主頁 | login、dashboard、主選單 | Auth、User、Dashboard、LoginStreak 呼叫 | `/api/auth/login`, `/api/users/me`, `/api/pets/login-tick` | `users`, `user_login_logs`, `pets` |
| 動畫處理 | pet-widget、寵物動畫、事件顯示 | Pet、PetEvent、AdminPetTest | `/api/pets/me`, `/api/pets/feed`, `/api/pets/events` | `pets`, `pet_events`, `pet_model` |
| Account 頁面 | accounts | Account | `/api/accounts` | `accounts` |
| 記帳主頁與明細 | transactions、transfers | Transaction、Transfer、DailyReward | `/api/transactions`, `/api/transfers`, `/api/rewards/daily/*` | `transactions`, `account_transactions`, `daily_record_rewards` |
| 預算與目標 | budget、goals | Budget、SavingGoal | `/api/budgets`, `/api/saving-goals` | `budget`, `saving_goals` |
| 圓餅圖顯示收支 | charts | Chart | `/api/charts/*` | `transactions`, `categories`, `accounts` |
| 管理員功能 | admin-users、admin-analysis、admin-pet-test | AdminAnalysis、AdminPetTest | `/api/admin/*` | 多表整合 |

---

# 8. 驗收與測試重點

| 測試 | 操作 | 預期結果 |
|---|---|---|
| T-01 | 註冊新使用者 | 建立 users、預設 accounts、categories、pets。 |
| T-02 | 新增支出 | transactions 新增、accounts.balance 扣款。 |
| T-03 | 新增第 1 筆交易 | daily_record_rewards 新增、pets.cancan +1、pet_events 新增 DAILY_BOOKKEEPING_REWARD。 |
| T-04 | 同日新增第 6 筆交易 | transaction_count = 6，但 cancanDelta 仍最多 5。 |
| T-05 | 修改交易日期 | 舊日期與新日期 DailyReward 都重新計算。 |
| T-06 | 刪除交易 | 帳戶餘額回沖，DailyReward 重算，但已發 cancan 不倒扣。 |
| T-07 | 餵食 CAN | cancan -1，mood +1，寫入 PET_FEED_CAN。 |
| T-08 | 同日一般餵食第 4 次 | cancan 可扣，但 mood 不再增加，或依實作回傳已達上限提示。 |
| T-09 | 餵食 FEAST | cancan -10，mood +15，不受一般餵食 +3 限制。 |
| T-10 | 同日重複 login-tick | 不重複新增 user_login_logs，不重複改 mood。 |
| T-11 | 連續登入 3 天 | 第 3 天 mood +5，pet_events 寫 LOGIN_STREAK_3。 |
| T-12 | 缺席 7 天登入 | mood -20，pet_events 寫 LOGIN_ABSENCE_7。 |
| T-13 | 查寵物事件 | 回傳 items，不包含 userId / petId。 |
| T-14 | Admin API 使用一般 token | 回傳權限不足。 |

---

# 9. 文件維護原則

1. SRS 只描述需求與驗收，不放過細 Java class 清單。
2. SA 聚焦使用情境、流程與資料流。
3. SD 聚焦模組責任、Service 邊界與前後端對應。
4. API 表只保留前端實作會用到的路徑、傳入、回傳與注意事項。
5. DTO / Repository / Entity 詳細欄位以實際程式碼與 SQL 為準，文件只保留必要對應與規則。
6. 若 API 實作修改，優先更新第 5 章 API 表與第 6 章前端注意事項。
