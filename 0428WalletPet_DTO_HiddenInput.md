# WalletPet DTO 最小化與 Hidden Input 對應評估表

版本：0428 DTO 最小化評估版  
目的：依目前 WalletPet 需求文件與 API 規格，整理哪些 DTO 建議保留、哪些可刪減，以及哪些資料若由前端 hidden input 帶入時，後端設計需明確註記給前端組員。

---

## 1. 調整原則

本版採用「盡量減少 class 數量」的做法，但不是完全不用 DTO。判斷原則如下：

| 判斷情境 | 建議做法 | 說明 |
|---|---|---|
| 涉及密碼、token、role、權限判斷 | 保留 DTO | 不建議直接回傳 `User` Entity，避免 password 等敏感欄位外洩。 |
| 回傳資料需要組合多張表 | 保留 Response DTO 或使用簡化 Map | 例如交易清單需同時回傳 accountName、categoryName，直接回 Entity 容易造成 Lazy Loading 或循環引用。 |
| 表單欄位與 Entity 幾乎一致 | 可不保留 Request DTO | 可用 `@RequestParam`、`@ModelAttribute` 或直接接收 Entity，但資料歸屬欄位仍應由後端 token 判斷。 |
| 只需要傳 id | 不必新增 DTO | 例如停用帳戶、刪除交易、查詢單筆資料，id 可放 PathVariable。 |
| 前端畫面需暫存 id | 前端可用 hidden input | 例如編輯交易時帶 `transactionId`、`accountId`、`categoryId`。後端需註記「只用於查詢目標資料，不可作為資料歸屬依據」。 |
| 分頁查詢或多條件查詢 | 不必新增 SearchRequest DTO | 建議使用 `@RequestParam`，例如 `page`、`size`、`startDate`、`endDate`。 |
| API 回傳格式需要一致 | 建議保留 `ApiResponse<T>` | 這是共用包裝格式，不建議刪除。 |

---

## 2. 最低建議保留 DTO 清單

若要減少 class 數量，建議保留以下最小集合。

| 類別 | 是否保留 | 原因 |
|---|---:|---|
| `ApiResponse<T>` | 保留 | 統一所有 API 成功 / 失敗格式。 |
| `PageResponse<T>` | 可選保留 | 若分頁 API 很多，保留可讓格式一致；若想再減少 class，可用 `Map<String, Object>` 回傳。 |
| `LoginRequest` | 保留 | 登入欄位與 Entity 不完全等同，且涉及密碼。 |
| `LoginResponse` | 保留 | 回傳 token、role、userId，不可直接回傳 User Entity。 |
| `RegisterRequest` | 保留 | 註冊同時需要 userName、password、petName，與 User Entity 不完全一致。 |
| `UserResponse` | 保留 | 避免回傳 password。 |
| `PetResponse` | 建議保留 | 首頁與動畫組常用，且需把 `mood` 說明成心情值、`cancan` 說明成食物量。 |
| `PetEventResponse` | 建議保留 | 明確不回傳 `userId`、`petId`，只給前端顯示事件紀錄。 |

---

## 3. DTO 保留 / 刪減總表

### 3.1 Auth / User

| 原建議 DTO | 建議 | 可替代做法 | 前端是否需 hidden input | 後端註記 |
|---|---|---|---|---|
| `LoginRequest` | 保留 | 無 | 否 | 登入只接收 `userName`、`password`。 |
| `LoginResponse` | 保留 | 無 | 否 | 回傳 token、role，不可回傳 password。 |
| `RegisterRequest` | 保留 | 無 | 否 | 註冊需額外帶 `petName`，不適合直接用 User Entity。 |
| `UserResponse` | 保留 | 無 | 否 | `/api/users/me` 不可直接回傳 User Entity，避免 password 外洩。 |

---

### 3.2 Account

| 原建議 DTO | 建議 | 可替代做法 | 前端 hidden input / 欄位需求 | 後端註記 |
|---|---|---|---|---|
| `AccountCreateRequest` | 可刪 | Controller 使用 `@RequestParam` 或接收 `Account` 部分欄位 | 不需 hidden；新增時輸入 `accountName`, `initialBalance`, `isLiability`, `isSavingAccount` | `userId` 一律從 token 取得，不可由前端傳入。 |
| `AccountUpdateRequest` | 可刪 | 使用 `@RequestParam` + `@PathVariable accountId` | 編輯表單需 hidden `accountId`，或使用 URL path `/accounts/{id}` | `accountId` 只用來找資料，仍需驗證該帳戶屬於目前登入者。 |
| `AccountResponse` | 可視情況保留 | 可直接回傳整理後的 `Map` 或 Entity 投影 | 否 | 若直接回 Entity，需避免回傳 `user` 物件造成循環引用。 |
| `AccountSummaryResponse` | 可刪 | 用 `Map<String, Object>` | 否 | 摘要資料是組合結果，若不建 DTO，可用 Map。 |

**前端需知道：**

| 畫面 | hidden input 建議 | 說明 |
|---|---|---|
| 編輯帳戶 | `accountId` | 修改或停用帳戶時帶入。 |
| 帳戶列表 | 不需 userId | 使用者身份由 token 判斷。 |
| 新增帳戶 | 不需 accountId | 由資料庫自動產生。 |

---

### 3.3 Category

| 原建議 DTO | 建議 | 可替代做法 | 前端 hidden input / 欄位需求 | 後端註記 |
|---|---|---|---|---|
| `CategoryCreateRequest` | 可刪 | 使用 `@RequestParam` 或接收簡化表單欄位 | 新增不需 hidden | `categoryId` 由 IdGenerator 產生。 |
| `CategoryUpdateRequest` | 可刪 | 使用 `@PathVariable categoryId` + `@RequestParam` | 編輯表單需 hidden `categoryId`，或使用 URL path | 系統分類 `isSystem=true` 應限制修改。 |
| `CategoryResponse` | 可選保留 | 可用 Entity 或 Map | 否 | 若 Entity 內含 User 關聯，避免直接序列化 user。 |
| `CategoryOptionResponse` | 可刪 | 用 `Map` 或只回傳 Category 的必要欄位 | 否 | `/categories/available` 只需給新增交易下拉選單。 |

**前端需知道：**

| 畫面 | hidden input 建議 | 說明 |
|---|---|---|
| 編輯分類 | `categoryId` | 修改分類名稱、icon、color、停用狀態時使用。 |
| 新增交易表單 | `categoryId` | 選擇分類後送出交易時必須帶入。 |
| 分類列表 | 不需 userId | 後端由 token 判斷資料歸屬。 |

---

### 3.4 Transaction

| 原建議 DTO | 建議 | 可替代做法 | 前端 hidden input / 欄位需求 | 後端註記 |
|---|---|---|---|---|
| `TransactionCreateRequest` | 可保留或可刪 | 若表單簡單，可用 `@RequestParam`；若用 JSON，保留 Request 較清楚 | 新增時需送 `accountId`, `categoryId` | `accountId`、`categoryId` 必須驗證屬於目前使用者且未停用。 |
| `TransactionUpdateRequest` | 可保留或可刪 | 使用 `@PathVariable transactionId` + `@RequestParam` | 編輯表單需 hidden `transactionId`、`accountId`、`categoryId` | 修改時需先還原舊交易影響，再套用新交易。 |
| `TransactionResponse` | 建議保留 | 可用 Map，但欄位多且跨表，DTO 較安全 | 否 | 需回傳 accountName、categoryName，不建議直接回 Entity。 |
| `TransactionSearchRequest` | 刪除 | 使用 QueryParam | 否 | `startDate`, `endDate`, `accountId`, `categoryId`, `type`, `page`, `size` 由 QueryParam 接收。 |
| `TransactionListResponse` | 可刪 | 使用 `Map<String,Object>` 包 summary、items、page | 否 | 若不保留 DTO，Controller 回傳 Map 即可。 |
| `TransactionSummaryResponse` | 可刪 | 使用 Map | 否 | 摘要欄位少，可不用 DTO。 |
| `MonthlyTransactionResponse` | 可刪 | 使用 Map | 否 | 月曆總覽是統計資料，可用 Map。 |
| `DailyTransactionResponse` | 可刪 | 使用 Map | 否 | 單日資料可用 Map。 |
| `TransactionFormMetaResponse` | 可刪 | 使用 Map：`accounts`, `categories` | 否 | 表單初始化資料可用 Map。 |
| `CalendarMarkerResponse` | 可刪 | 使用 Map 或 List<Map> | 否 | 日期標記欄位簡單，可不用 DTO。 |

**前端需知道：**

| 使用位置 | hidden input / 表單欄位 | 說明 |
|---|---|---|
| 新增交易 | `accountId`, `categoryId` | 從下拉選單選到的 id。 |
| 編輯交易 | `transactionId`, `accountId`, `categoryId` | `transactionId` 可 hidden；`accountId`、`categoryId` 由目前選項帶入。 |
| 刪除交易 | `transactionId` | 建議用 PathVariable `/api/transactions/{id}`。 |
| 查詢交易 | 不需 hidden | 使用 QueryParam。 |

---

### 3.5 Transfer / AccountTransaction

| 原建議 DTO | 建議 | 可替代做法 | 前端 hidden input / 欄位需求 | 後端註記 |
|---|---|---|---|---|
| `TransferCreateRequest` | 可刪 | 使用 `@RequestParam` | 新增時需送 `fromAccountId`, `toAccountId` | 兩個帳戶都需驗證屬於目前使用者，且不可相同。 |
| `TransferResponse` | 建議保留 | 可用 Map | 否 | 回傳來源 / 目標帳戶名稱與餘額，跨兩個 Account，DTO 較清楚。 |
| `TransferSearchRequest` | 刪除 | 使用 QueryParam | 否 | `startDate`, `endDate`, `fromAccountId`, `toAccountId`, `page`, `size`。 |
| `TransferListResponse` | 可刪 | 使用 Map | 否 | 總覽明細可用 Map 包 summary 與 items。 |
| `TransferSummaryResponse` | 可刪 | 使用 Map | 否 | 摘要欄位少。 |

**前端需知道：**

| 使用位置 | hidden input / 表單欄位 | 說明 |
|---|---|---|
| 新增轉帳 | `fromAccountId`, `toAccountId` | 由下拉選單選擇。 |
| 查詢轉帳明細 | 不需 hidden | 使用 QueryParam。 |
| 若未來做轉帳單筆詳情 | `accountTransId` | 可 hidden 或放 URL path。 |

---

### 3.6 Budget

| 原建議 DTO | 建議 | 可替代做法 | 前端 hidden input / 欄位需求 | 後端註記 |
|---|---|---|---|---|
| `BudgetCreateRequest` | 可刪 | 使用 `@RequestParam` | 若分類預算需送 `categoryId` | SQL 目前部分欄位與文件不完全一致，例如 `budgetName`, `alertThreshold`，需先確認是否補欄位。 |
| `BudgetUpdateRequest` | 可刪 | 使用 `@PathVariable budgetId` + `@RequestParam` | 編輯預算需 hidden `budgetId` | 修改後即時計算使用率。 |
| `BudgetResponse` | 建議保留或 Map | 因需組合 spentAmount、remainingAmount、usageRate，可用 DTO 或 Map | 否 | 不建議直接回 Budget Entity，因使用率不是資料表欄位。 |
| `BudgetUsageResponse` | 可刪 | 使用 Map | 否 | 單純使用率查詢可 Map。 |

**前端需知道：**

| 使用位置 | hidden input / 表單欄位 | 說明 |
|---|---|---|
| 建立分類預算 | `categoryId` | 若 `budgetScope=CATEGORY` 時必填。 |
| 編輯預算 | `budgetId` | 可 hidden 或放 URL path。 |
| 刪除預算 | `budgetId` | 建議 PathVariable。 |

---

### 3.7 SavingGoal

| 原建議 DTO | 建議 | 可替代做法 | 前端 hidden input / 欄位需求 | 後端註記 |
|---|---|---|---|---|
| `SavingGoalCreateRequest` | 可刪 | 使用 `@RequestParam` | 建立時需送 `accountId` | 建議只允許選擇 `isSavingAccount=true` 的帳戶。 |
| `SavingGoalUpdateRequest` | 可刪 | 使用 `@PathVariable savingGoalId` + `@RequestParam` | 編輯時需 hidden `savingGoalId` | 不可由前端傳 userId。 |
| `SavingGoalResponse` | 建議保留或 Map | 因需計算 currentAmount、progressRate | 否 | Entity 不直接有 progressRate，建議 DTO 或 Map。 |
| `SavingGoalDepositRequest` | 可刪 | 使用 `@RequestParam` | 需送 `savingGoalId`, `fromAccountId` | `savingGoalId` 建議 PathVariable，`fromAccountId` 由表單選擇。 |
| `SavingGoalWithdrawRequest` | 可刪 | 使用 `@RequestParam` | 需送 `savingGoalId`, `toAccountId` | 提取金額不可大於目前已存。 |
| `SavingGoalActionResponse` | 可刪 | 使用 Map | 否 | 存入 / 提取結果可 Map。 |

**前端需知道：**

| 使用位置 | hidden input / 表單欄位 | 說明 |
|---|---|---|
| 建立目標 | `accountId` | 綁定的存款帳戶 id。 |
| 編輯目標 | `savingGoalId` | 可 hidden 或 URL path。 |
| 存錢到目標 | `savingGoalId`, `fromAccountId` | `savingGoalId` 建議用 URL path，`fromAccountId` 由選單帶入。 |
| 從目標提取 | `savingGoalId`, `toAccountId` | `toAccountId` 是錢要轉回的帳戶。 |

---

### 3.8 Chart / Dashboard

| 原建議 DTO | 建議 | 可替代做法 | 前端 hidden input / 欄位需求 | 後端註記 |
|---|---|---|---|---|
| `ExpensePieResponse` | 可刪 | 使用 List<Map> | 否 | 統計資料欄位固定，可 Map。 |
| `CashflowLineResponse` | 可刪 | 使用 List<Map> | 否 | 折線圖資料可直接回 label、income、expense、balance。 |
| `MonthlyCashflowResponse` | 可刪 | 使用 List<Map> | 否 | 月份統計可 Map。 |
| `DailyCashflowResponse` | 可刪 | 使用 List<Map> | 否 | 日期統計可 Map。 |
| `ChartSummaryResponse` | 可刪 | 使用 Map | 否 | 摘要資料欄位少。 |
| `DashboardResponse` | 可刪 | 使用 Map | 否 | 首頁彙整資料來源多，DTO 很大，可用 Map 減少 class。 |

**前端需知道：**

| 查詢項目 | QueryParam | 說明 |
|---|---|---|
| 圓餅圖 | `startDate`, `endDate`, `accountId` | `accountId` 可選。 |
| 折線圖 | `startDate`, `endDate`, `groupBy` | groupBy 可為 DAY / MONTH。 |
| 月摘要 | `year`, `month` | 不需 hidden。 |

---

### 3.9 Pet / Reward

| 原建議 DTO | 建議 | 可替代做法 | 前端 hidden input / 欄位需求 | 後端註記 |
|---|---|---|---|---|
| `PetResponse` | 保留 | 不建議直接回 Pet Entity | 否 | 對前端說明：`mood` = 心情值，`cancan` = 食物量。 |
| `PetInteractRequest` | 可刪 | 使用 `@RequestParam interactionType` | 否 | 點擊互動不需另外 class。 |
| `PetInteractResponse` | 可刪 | 使用 Map | 否 | 回傳動畫與寵物狀態即可。 |
| `PetFeedRequest` | 可刪 | 使用 `@RequestParam foodType` | 否 | foodType 可為 CAN / FISH / SNACK / FEAST。 |
| `PetFeedResponse` | 可刪或保留 | 使用 Map | 否 | 若動畫組需要穩定欄位，建議保留；否則 Map。 |
| `PetEventResponse` | 保留 | 不建議直接回 PetEvent Entity | 否 | 明確不回傳 userId、petId。 |
| `BookkeepingRewardRequest` | 可刪 | 使用 `@RequestParam rewardDate` | 否 | rewardDate 未傳時預設今天。 |
| `BookkeepingRewardResponse` | 可刪或保留 | 使用 Map | 否 | 若前端只顯示食物量增加結果，可 Map。 |
| `LoginTickRequest` | 可刪 | 使用後端今天日期，測試時才開 `loginDate` QueryParam | 否 | 正式環境不建議讓前端任意指定 loginDate。 |
| `LoginTickResponse` | 可刪或保留 | 使用 Map | 否 | 若登入動畫需穩定欄位，可保留。 |
| `DailyRewardResponse` | 可刪 | 使用 Map | 否 | 歷史紀錄可用 List<Map>。 |

**前端需知道：**

| 使用位置 | hidden input / 表單欄位 | 說明 |
|---|---|---|
| 查看我的寵物 | 不需 petId | `/api/pets/me` 根據 token 找目前顯示寵物。 |
| 餵食 | `foodType` | 不需 petId；後端會找目前使用者的顯示寵物。 |
| 查看寵物事件 | 不需 userId / petId | 後端由 token 判斷。 |
| 管理員手動改心情值 | `petId` | Admin 功能需要知道被修改的寵物 id。 |

---

### 3.10 Admin

| 原建議 DTO | 建議 | 可替代做法 | 前端 hidden input / 欄位需求 | 後端註記 |
|---|---|---|---|---|
| `AdminSystemDataResponse` | 可刪 | 使用 Map | 否 | 系統資料統計可 Map。 |
| `AdminSystemDataUpdateRequest` | 可刪 | 使用 `@RequestParam` 或 Map body | 視更新類型而定 | 若尚無 system_config 表，可先不實作。 |
| `AdminUserOverviewResponse` | 建議保留或 Map | 用戶總覽欄位多，保留 DTO 較清楚；若要減少 class 可 Map | 否 | 顯示每個用戶本月支出、收入、記帳筆數等。 |
| `AdminUserAnalysisResponse` | 建議使用 Map | 詳細分析包含多個圖表與列表，DTO 會很大 | 前端點選用戶時需帶 `userId` | Admin 可查指定 userId，但需驗證 role=ADMIN。 |
| `AdminGroupAnalysisResponse` | 建議使用 Map | 群體分析是統計結果，可 Map | 否 | 只限 ADMIN。 |
| `AdminPetMoodUpdateRequest` | 可刪 | 使用 `@PathVariable petId` + `@RequestParam mood` | 管理員測試頁需帶 `petId` | 手動修改心情值主要供動畫測試，需寫入 `pet_events`。 |

**前端需知道：**

| 管理員功能 | hidden input / 欄位 | 說明 |
|---|---|---|
| 用戶總覽 | 不需 hidden | 查詢所有用戶統計。 |
| 用戶詳細分析 | `userId` | 從用戶總覽點入時帶入。 |
| 群體分析 | 不需 hidden | 查詢全體統計。 |
| 寵物動畫測試 | `petId`, `mood` | 管理員選定寵物並手動設定心情值。 |

---

## 4. 建議保留的後端 Class 最小版

若專案要明顯減少 class，建議最小保留如下。

### 4.1 Entity 必留

| 類別 | 原因 |
|---|---|
| `User` | 對應 users。 |
| `Account` | 對應 accounts。 |
| `Category` | 對應 categories。 |
| `Transaction` | 對應 transactions。 |
| `AccountTransaction` | 對應 account_transactions。 |
| `Budget` | 對應 budget。 |
| `SavingGoal` | 對應 saving_goals。 |
| `PetModel` | 對應 pet_model。 |
| `Pet` | 對應 pets。 |
| `PetEvent` | 對應 pet_events。 |
| `DailyRecordReward` | 對應 daily_record_rewards。 |
| `UserLoginLog` | 對應 user_login_logs。 |

### 4.2 Repository 必留

| 類別 | 原因 |
|---|---|
| `UserRepository` | 登入、註冊、管理員分析。 |
| `AccountRepository` | 帳戶、交易、轉帳、存錢目標。 |
| `CategoryRepository` | 分類、交易、圖表。 |
| `TransactionRepository` | 收支明細、圖表、預算、每日獎勵。 |
| `AccountTransactionRepository` | 轉帳。 |
| `BudgetRepository` | 預算與超支分析。 |
| `SavingGoalRepository` | 存錢目標。 |
| `PetModelRepository` | 初始寵物模型。 |
| `PetRepository` | 寵物狀態與管理員測試。 |
| `PetEventRepository` | 寵物事件與心情值趨勢。 |
| `DailyRecordRewardRepository` | 每日記帳獎勵。 |
| `UserLoginLogRepository` | 登入 streak。 |

### 4.3 DTO 最小保留

| 類別 | 保留原因 |
|---|---|
| `ApiResponse<T>` | 統一 API 格式。 |
| `LoginRequest` | 登入欄位獨立。 |
| `LoginResponse` | 回傳 token，不可直接回 User。 |
| `RegisterRequest` | 註冊需要 petName。 |
| `UserResponse` | 避免 password 外洩。 |
| `PetResponse` | 前端寵物狀態欄位穩定。 |
| `PetEventResponse` | 避免回傳 userId、petId，且事件顯示欄位固定。 |
| `TransactionResponse` | 交易回傳跨 Account / Category，保留較穩定。 |
| `TransferResponse` | 轉帳回傳跨兩個 Account，保留較穩定。 |

其餘 Request / Response DTO 可先不建立，使用 `@RequestParam`、`@PathVariable`、`Map<String,Object>` 或前端 hidden input 配合。

---

## 5. Controller 寫法建議

### 5.1 新增交易：不使用 Request DTO

```java
@PostMapping("/api/transactions")
public ApiResponse<TransactionResponse> createTransaction(
        @RequestParam String transactionType,
        @RequestParam Integer accountId,
        @RequestParam String categoryId,
        @RequestParam BigDecimal transactionAmount,
        @RequestParam LocalDate transactionDate,
        @RequestParam(required = false) String note
) {
    String currentUserId = authService.getCurrentUserId();
    return ApiResponse.success(
        "新增交易成功",
        transactionService.createTransaction(currentUserId, transactionType, accountId, categoryId, transactionAmount, transactionDate, note)
    );
}
```

前端表單需送：

| 欄位 | 來源 |
|---|---|
| `transactionType` | 收入 / 支出按鈕或 select。 |
| `accountId` | 帳戶下拉選單。 |
| `categoryId` | 分類下拉選單。 |
| `transactionAmount` | 金額輸入框。 |
| `transactionDate` | 日期輸入框。 |
| `note` | 備註輸入框，可空白。 |

---

### 5.2 編輯交易：使用 PathVariable + hidden input

```java
@PutMapping("/api/transactions/{id}")
public ApiResponse<TransactionResponse> updateTransaction(
        @PathVariable("id") String transactionId,
        @RequestParam String transactionType,
        @RequestParam Integer accountId,
        @RequestParam String categoryId,
        @RequestParam BigDecimal transactionAmount,
        @RequestParam LocalDate transactionDate,
        @RequestParam(required = false) String note
) {
    String currentUserId = authService.getCurrentUserId();
    return ApiResponse.success(
        "交易修改成功",
        transactionService.updateTransaction(currentUserId, transactionId, transactionType, accountId, categoryId, transactionAmount, transactionDate, note)
    );
}
```

前端可用：

```html
<input type="hidden" name="transactionId" value="TXN202604270001">
```

但若 API URL 已是 `/api/transactions/{id}`，前端送出時應把 hidden 的 `transactionId` 放進 URL，而不是另外在 body 傳一次。

---

### 5.3 管理員手動修改寵物心情值

```java
@PutMapping("/api/admin/pets/{petId}/mood")
public ApiResponse<Map<String, Object>> updatePetMoodForTest(
        @PathVariable String petId,
        @RequestParam Integer mood,
        @RequestParam(required = false) String reason
) {
    authService.requireAdmin();
    return ApiResponse.success(
        "寵物心情值已更新",
        adminService.updatePetMoodForTest(petId, mood, reason)
    );
}
```

前端需提供：

| 欄位 | 說明 |
|---|---|
| `petId` | 被測試的寵物 id。 |
| `mood` | 管理員手動設定的心情值。 |
| `reason` | 可選，記錄測試原因。 |

---

## 6. 前端 Hidden Input 總整理

| 模組 | 情境 | hidden input / 表單欄位 | 是否可由前端傳 userId | 後端驗證 |
|---|---|---|---:|---|
| Account | 編輯 / 停用帳戶 | `accountId` | 否 | 驗證帳戶屬於 currentUserId。 |
| Category | 編輯 / 停用分類 | `categoryId` | 否 | 驗證分類屬於 currentUserId，且系統分類不可任意改。 |
| Transaction | 新增交易 | `accountId`, `categoryId` | 否 | 驗證帳戶與分類屬於 currentUserId 且可用。 |
| Transaction | 編輯 / 刪除交易 | `transactionId` | 否 | 驗證交易屬於 currentUserId。 |
| Transfer | 新增轉帳 | `fromAccountId`, `toAccountId` | 否 | 驗證兩個帳戶都屬於 currentUserId 且不可相同。 |
| Budget | 分類預算 | `categoryId` | 否 | 驗證分類屬於 currentUserId。 |
| Budget | 編輯 / 刪除預算 | `budgetId` | 否 | 驗證預算屬於 currentUserId。 |
| SavingGoal | 建立目標 | `accountId` | 否 | 驗證帳戶屬於 currentUserId 且為存款帳戶。 |
| SavingGoal | 編輯 / 刪除 / 完成 | `savingGoalId` | 否 | 驗證目標屬於 currentUserId。 |
| SavingGoal | 存錢 | `savingGoalId`, `fromAccountId` | 否 | 驗證目標與來源帳戶皆屬於 currentUserId。 |
| SavingGoal | 提取 | `savingGoalId`, `toAccountId` | 否 | 驗證目標與接收帳戶皆屬於 currentUserId。 |
| Pet | 餵食 | `foodType` | 否 | 後端自行找目前顯示寵物。 |
| Pet | 登入 tick | 無 | 否 | 後端使用今天日期與 currentUserId。 |
| Admin | 用戶詳細分析 | `userId` | 管理員功能例外，可指定被查詢 userId | 必須驗證 role=ADMIN。 |
| Admin | 寵物心情值測試 | `petId`, `mood` | 管理員功能例外，可指定 petId | 必須驗證 role=ADMIN，並寫入 pet_events。 |

---

## 7. 組員評估用決策表

| 類別 | 建議決策 | 原因 |
|---|---|---|
| Entity | 全部保留 | 對應資料表，JPA 必要。 |
| Repository | 全部保留 | 對應資料查詢，Service 需要使用。 |
| Mapper | 可大量刪減 | 若回傳欄位簡單，可直接在 Service / Controller 組 Map；但 Transaction、PetEvent 可保留 Mapper。 |
| Request DTO | 大多可刪 | 表單欄位可用 `@RequestParam` 接收。 |
| Response DTO | 統計類可刪 | 圖表、摘要、Dashboard 可用 Map。 |
| 敏感資料 DTO | 必留 | User / Login 類不可直接回 Entity。 |
| 跨表回傳 DTO | 視情況保留 | Transaction、Transfer、PetEvent 建議保留，避免 Entity 關聯序列化問題。 |
| Hidden input | 可使用 | 只用來帶 id，不可帶 userId 作為資料歸屬判斷。 |

---

## 8. 最終建議

本專案若以「能清楚分工、減少 class、降低組員理解負擔」為優先，建議採用以下折衷版本：

1. **Entity 與 Repository 全部保留。**
2. **DTO 只保留安全性與跨表回傳必要項目。**
3. **新增 / 修改表單類 Request DTO 大多刪除，改用 `@RequestParam`。**
4. **查詢條件類 DTO 全部刪除，改用 QueryParam。**
5. **圖表 / 統計 / Dashboard 類 Response DTO 可刪，改用 `Map<String,Object>` 或 `List<Map<String,Object>>`。**
6. **前端可以使用 hidden input 帶各資料 id，但不可帶 userId 作為資料歸屬依據。**
7. **後端所有修改、刪除、查詢單筆資料都必須驗證該資料是否屬於目前登入者。**

建議最終保留 DTO：

```text
ApiResponse<T>
LoginRequest
LoginResponse
RegisterRequest
UserResponse
PetResponse
PetEventResponse
TransactionResponse
TransferResponse
```

其餘 DTO 先不建立，等功能變複雜或前端需要固定格式時再補。
