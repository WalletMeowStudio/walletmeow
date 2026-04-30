# walletpet

> ## 🎯 此分支為主開發版本（後端串接版）
> `main` = 前端 + Spring Boot 後端 + MySQL 完整串接版本，是專案的**主要實作路線**。
> - 後端課程相關開發（Auth / JPA / Controller / Service）一律在此分支進行
> - 想做純瀏覽器版（無後端、單機跑），請切換到 [`frontend-only`](https://github.com/WalletMeowStudio/walletpet/tree/frontend-only) branch
> - 想了解未來「雙模式整合版」規劃，請切換到 [`duo`](https://github.com/WalletMeowStudio/walletpet/tree/duo) branch（規劃中）

### 🌳 三條長期分支角色分工

| branch | 角色 | 資料來源 | 部署目標 | 狀態 |
|---|---|---|---|---|
| **`main`** ⭐ 你在這 | 後端串接版 + Spring Boot | MySQL（透過 REST API）| Railway / 內部伺服器 | ✅ 開發中 |
| [`frontend-only`](https://github.com/WalletMeowStudio/walletpet/tree/frontend-only) | 純瀏覽器版（純前端） | localStorage / IndexedDB | Netlify 靜態託管 | ✅ 開發中 |
| [`duo`](https://github.com/WalletMeowStudio/walletpet/tree/duo) | 雙模式整合版（flag 切換） | 兩種皆可 | 兩種部署目標皆可 | 🌱 規劃中 |

> **三分支獨立、互不合併**。`duo` 將以 `main` 為基底新開、人工複製 frontend-only 程式碼進去（不用 git merge），維持三條長期分支各自演進。詳見 [§11.5 duo branch（未來規劃）](#115-duo-branch未來規劃)。

---

記帳 × 養寵物 的全端專案。前端為靜態 HTML/JS（[walletpet-frontend/](walletpet-frontend/)），後端為 Spring Boot + MySQL（[walletpet-backend/](walletpet-backend/)）。

> **規格依據**：本文件以 `0425WalletPet 規格資料夾` 內的 `WalletPet_SA_SD_SRS_需求文件.docx` 與 `需求書4-5API詳細內容.docx` 為唯一基準。所有 API 命名、欄位、驗證規則皆以該規格為準。

---

## 📑 目錄

- [§1 架構速覽](#1-架構速覽)
- [§2 待修正事項總覽（4 張表）](#2-待修正事項總覽)
  - [2.1 SQL 待修正事項](#21-sql-待修正事項)
  - [2.2 後端待修正事項](#22-後端待修正事項)
  - [2.3 API（api.js）待修正事項](#23-apiapijs-待修正事項)
  - [2.4 前端（UI / HTML）待修正事項](#24-前端ui--html-待修正事項)
- [§3 API 三向對照（規格 / 前端 / 後端）](#3-api-三向對照規格--前端--後端)
- [§4 後端目前實作了什麼](#4-後端目前實作了什麼)
- [§5 待組員討論的規格灰色地帶（D1 / D2 / D3）](#5-待組員討論的規格灰色地帶)
- [§6 詳細落差分析（P0 / P1 / P2）](#6-詳細落差分析-p0--p1--p2)
- [§7 SQL Schema 詳細問題（S1–S27）](#7-sql-schema-詳細問題-s1s27)
- [§8 串接順序建議](#8-串接順序建議)
- [§9 本機啟動](#9-本機啟動)
- [§10 跨平台執行方案（PC / Android / iOS · 全本地）](#10-跨平台執行方案)
  - [10.4 HTTPS — iOS 安裝 PWA 的硬性要求](#104-https--ios-安裝-pwa-的硬性要求)
  - [10.9 §10.4 A 方案（Spring Boot 接管前端 + SSL）實作計畫](#109-104-a-方案實作計畫下一步預定執行)
- [§11 部署策略選擇（A 雲端 / B LAN / C Docker · D 純前端 → `frontend-only` branch）](#11-部署策略選擇)
  - [11.1 三項主要選擇比較](#111-三項主要選擇比較)
  - [11.3 建議組合（職訓班場景）](#113-建議組合職訓班場景)
  - [11.4 純前端緊急 fallback（Option D · 已分流到 branch）](#114-純前端緊急-fallbackoption-d)
  - [11.5 duo branch（未來規劃）— 雙模式整合版](#115-duo-branch未來規劃)

---

## 1. 架構速覽

| 項目 | 內容 |
|---|---|
| 前端 | 純 HTML + `shared.js` + `api.js`（無打包工具），預設打 `http://localhost:8080` |
| 後端 | Spring Boot，預設埠 8080，DB = MySQL `walletpet`（`hbm2ddl.auto=validate`，表必須先用 [walletpet.sql](walletpet.sql) 建好） |
| Auth | 規格 4.5.2：除 `/api/auth/login` 外**全部端點皆需 JWT**，後端從 token 取 `currentUserId`，**不接受前端傳 userId** |
| Resp 格式 | 規格 4.5.1：`{ success, message, data, errorCode? }` |
| CORS | 後端只放行 `localhost:5173 / 127.0.0.1:5173 / localhost:4200` |
| SQL 版本 | 0424（commit `6aaac95`，含 `daily_record_rewards`、`pet_model` 等表） |

---

## 📖 讀者地圖

> 第一次看建議照順序 §1 → §11；已熟悉的成員可直接跳目標區段。

| 你的目的 | 看哪幾節 |
|---|---|
| 快速了解專案 | [§1 架構速覽](#1-架構速覽) → [§9 本機啟動](#9-本機啟動) |
| 看修正進度 / 找 issue 認領 | [§2 待修正事項總覽](#2-待修正事項總覽) 4 張表 |
| 接手 API 串接 | [§3 API 三向對照](#3-api-三向對照規格--前端--後端) → [§4 後端目前實作了什麼](#4-後端目前實作了什麼) |
| 規格灰色地帶討論 | [§5 待組員討論的規格灰色地帶](#5-待組員討論的規格灰色地帶) |
| SQL 詳細問題 | [§7 SQL Schema 詳細問題](#7-sql-schema-詳細問題-s1s27) |
| **準備 demo / 部署** | [§11 部署策略選擇](#11-部署策略選擇) → [§10 跨平台執行方案](#10-跨平台執行方案)（B / LAN 實作細節） |
| 改 LAN HTTPS 設定 | [§10.4 HTTPS](#104-https--ios-安裝-pwa-的硬性要求) → [§10.9 A 方案實作 SOP](#109-104-a-方案實作計畫下一步預定執行) |

---

## 2. 待修正事項總覽

> 🔴 P0 = 阻塞項，沒做就跑不起來｜🟠 P1 = 上線前必補｜🟡 P2 = 體驗 / 命名 / 規範
> 灰 = 待團隊決議的灰色地帶（詳見 §5）

### 2.1 SQL 待修正事項

| 優先級 | 編號 | 待修內容 | 規格依據 / 原因 | 詳細 |
|---|---|---|---|---|
| 🔴 P0 | S24 | 加回 `accounts.currency_code` 或正式決議放棄多幣別 | 規格 4.4 / 4.5 仍要求 currencyCode | [§7 S24](#-0424-版本特有問題) |
| 🔴 P0 | S22 | `budget` 加 `budget_name` / `alert_threshold` | 規格 4.5 `POST /api/budgets` body 要求 | [§7 S22](#-p1--entity--業務邏輯對不上) |
| 🔴 P0 | S3 | 密碼明文 → BCrypt hash + seed 重灌 | 資安 | [§7 S3](#-p0--直接影響登入--認證) |
| 🟠 P1 | S2 | `users` 加 `email` 或確認用 user_name 登入 | 設計待確認 | [§7 S2](#-p0--直接影響登入--認證) |
| 🟠 P1 | S4 | 加入 demo user（一般使用者帳號） | 測試需求 | [§7 S4](#-p0--直接影響登入--認證) |
| 🟠 P1 | S6 | `saving_goals` 業務約束：account.is_saving_account=1 | 規格 4.5 暗示 | [§7 S6](#-p1--entity--業務邏輯對不上) |
| 🟠 P1 | S9 | `pets` 加 `UNIQUE(user_id) WHERE is_displayed=1` | 規格 1:N + 展示中只能 1 隻 | [§7 S9](#-p1--entity--業務邏輯對不上) |
| 🟠 P1 | S12 | `mood` 加 `CHECK (0–100)` 上下限 | 業務正確性 | [§7 S12](#-p1--entity--業務邏輯對不上) |
| 🟠 P1 | S17 | `categories` 加 `UNIQUE(user_id, name, type)` | 避免下拉選單重複 | [§7 S17](#-p2--命名--規範--索引問題) |
| 🟠 P1 | S21+ | `pet_model` 補 seed（至少一筆預設 model） | 0424 已建表但無 seed | [§7 S21](#-p1--entity--業務邏輯對不上) |
| 🟠 P1 | FR-04 | 新使用者預設資料邏輯（現金 / 銀行帳戶 + 初始 pet mood=80） | 規格 FR-04 | — |
| 🟡 P2 | S5 | 統一 PK 型別（全 varchar 或全 auto-increment）⏱️ **約 1–2 天**（牽動全 Entity / Repository / FK） | 維護性 | [§7 S5](#-p1--entity--業務邏輯對不上) |
| 🟡 P2 | S7 / S8 | account_transactions / transactions 跨幣別欄位 | 多幣別支援 | [§7 S7-S8](#-p1--entity--業務邏輯對不上) |
| 🟡 P2 | S10 / S11 | categories 系統分類查詢邏輯 + icon `'wait'` placeholder | 業務邏輯定義 | [§7 S10-S11](#-p1--entity--業務邏輯對不上) |
| 🟡 P2 | S13–S15 | `budget` 表名單數 + 欄位命名 + ENUM 約束 | 命名規範 | [§7 S13-S15](#-p2--命名--規範--索引問題) |
| 🟡 P2 | S16 | account_transactions 補 `transaction_date` 索引 | 效能 | [§7 S16](#-p2--命名--規範--索引問題) |
| 🟡 P2 | S18 | transactions / 其他表加 `deleted_at` 軟刪除 ⏱️ **約 1 天**（全表 + Repository filter + cascade 重審） | 規格 UC-05 / UC-19 暗示 | [§7 S18](#-p2--命名--規範--索引問題) |
| 🟡 P2 | S19 | 時區處理規範（UTC vs LocalDateTime）⏱️ **約 1–2 天**（全 codebase 替換 + DTO 格式 + 前端） | 跨時區部署 | [§7 S19](#-p2--命名--規範--索引問題) |
| 🟡 P2 | S23 | `pet_events.event_type` 補 enum 註解 | 業務正確性 | [§7 S23](#-p1--entity--業務邏輯對不上) |
| 🟡 P2 | S26 | `saving_goals.final_account_name` 設計（FK or snapshot） | 資料完整性 | [§7 S26](#-0424-版本特有問題) |
| 🟡 P2 | S27 | `daily_record_rewards.reward_type` enum 約束 | 業務正確性 | [§7 S27](#-0424-版本特有問題) |

### 2.2 後端待修正事項

| 優先級 | 模組 / 項目 | 內容 | 規格依據 |
|---|---|---|---|
| 🔴 P0 | **Auth + JWT Filter** | `POST /api/auth/login` + Spring Security + JWT 簽發 / 驗證 Filter ⏱️ **約 1–2 天**（Spring Security 0→1 + JWT lib 串接） | 規格 4.5 + 4.5.2 |
| 🔴 P0 | **Pet 改造** | 砍掉 `?userId=` 與 `/{petId}` CRUD，改 `/me` + `/interact` + `/reward-by-bookkeeping` | 規格 4.5 + 4.5.2 |
| 🔴 P0 | **User /me** | `GET /api/users/me`（從 JWT 取 userId） | 規格 4.5 |
| 🟠 P1 | **Account 模組** | 5 隻 CRUD + Entity（含 `currency_code` 待 S24 決議） | 規格 4.5 |
| 🟠 P1 | **Category 模組** | 5 隻 CRUD + Entity + `/available` | 規格 4.5 |
| 🟠 P1 | **Transaction 模組** | 主 CRUD + 5 隻聚合（`/monthly` `/daily` `/form-meta` `/summary` `/calendar-markers`）⏱️ **約 1–2 天**（聚合 SQL / JPQL 不簡單） | 規格 4.5 |
| 🟠 P1 | **Transfer 模組** | 單一 `POST /api/transfers` + Entity | 規格 4.5 |
| 🟠 P1 | **Budget 模組** | 6 隻（CRUD + `/{id}/usage`） + Entity（待 S22 SQL 同步） | 規格 4.5 |
| 🟠 P1 | **SavingGoal 模組** | 7 隻（CRUD + deposit + withdraw + complete） | 規格 4.5 |
| 🟠 P1 | **Chart 模組** | 7 隻聚合查詢（pie / cashflow-line / monthly-cashflow / daily-cashflow / summary / monthly-summary / daily-summary）⏱️ **約 1.5–2.5 天**（每隻時間維度與 group by 都不同） | 規格 4.5 |
| 🟠 P1 | **Admin 模組** | 2 隻（system-data GET / PUT）+ `role=ADMIN` 驗證 | 規格 4.5 |
| 🟠 P1 | **PetModel Entity** | 對應 0424 新表 `pet_model` | hbm2ddl validate |
| 🟠 P1 | application-local.properties | DB 帳密本機覆蓋 + `.gitignore` 規則 | 不洩 root/1234 |
| 🟠 P1 | CORS 擴充 | 加 `:5500`（VS Code Live Server）至白名單 | 開發體驗 |
| 🟠 P1 | DB schema 啟動依賴 | `hbm2ddl.auto=validate` 需先灌 SQL，不然啟動失敗 | 後端啟動穩定性 |
| 🟡 P2 | `server.port` 寫死 | application.properties 明確設 8080 避免衝突 | 環境一致性 |
| 🟡 P2 | `ApiResponse` 補 `errorCode` | 規格 4.5.1 共用回傳格式要求 | 規格符合 |
| 🟡 P2 | `message` 統一中文 | 目前中英文混用 | 一致性 |
| 🟡 P2 | `WalletPetBackendApplication` 改名 | 品牌大小寫一致（含 `*Tests.java`） | 命名規範 |
| 🟡 P2 | OpenAPI / Swagger | springdoc-openapi-ui，用規格 4.5 範例當 example | 文件協作 |
| 🟡 P2 | reward-by-bookkeeping 業務邏輯 | 流程 D-1：判斷今日首次 → 寫 `DAILY_BOOKKEEPING_REWARD` + `daily_record_rewards` → 更新 `pets.mood` | 規格 FR-04 |

### 2.3 API（api.js）待修正事項

| 優先級 | 項目 | 內容 | 規格依據 |
|---|---|---|---|
| 🟠 P1 | `transactionApi` 補 5 隻 | `/monthly` `/daily` `/form-meta` `/summary` `/calendar-markers` | 規格 4.5 |
| 🟠 P1 | `categoryApi` 補 `/available` | 啟用中分類列表 | 規格 4.5 |
| 🟠 P1 | `budgetApi` 補 `/{id}/usage` | 預算使用率 | 規格 4.5 |
| 🟠 P1 | `savingGoalApi` 補 `/withdraw` `/complete` | 提取 / 完成目標 | 規格 4.5 |
| 🟠 P1 | 新增 `adminApi` | system-data GET / PUT | 規格 4.5 |
| 🟡 P2 | `authApi` 移除 `register` | 規格未定義；同步清掉 login.html alert 占位 | 規格 4.5 |
| 🟡 P2 | login.html 寫入 token | 登入成功後 `setToken(jwt)` + `localStorage.walletpet.user` | 規格 4.5.2 流程 |
| 🟡 P2 | `shared.js` 401 攔截 | 偵測 401 → 清 token + 導回 login | 認證流程 |
| 🟡 P2 | `shared.js` network error | [shared.js:121](walletpet-frontend/shared.js#L121) `res.status === 0` 偵測無效，改用 try/catch | 程式正確性 |
| 🟡 P2 | `BASE_URL` 可設定 | 從 `localStorage` 或 `<meta>` 讀，不要寫死 | 部署彈性 |
| 灰 | D1 transferApi list | 規格無 list 端點 → 用 transactions 篩 / 補規格 / 砍 UI 三選一 | [§5 D1](#d1-transfers-頁的歷史列表怎麼來) |
| 灰 | D2 petApi.interact 值 | `PET / MUSIC / ACHIEVEMENT / CHAT` vs 規格 `CLICK / FEED / PLAY` | [§5 D2](#d2-pet-interact-的合法值規格枚舉-vs-現有按鈕) |

### 2.4 前端（UI / HTML）待修正事項

| 優先級 | 頁面 / 範圍 | 內容 |
|---|---|---|
| 🔴 P0 | login.html 流程 | 登入成功要寫入 JWT + user 資訊到 localStorage |
| 🟠 P1 | 401 處理 | `shared.js` 攔截 + 自動導回 login |
| 🟠 P1 | dashboard.html 真資料接通 | 依賴 Chart 7 隻 API |
| 🟠 P1 | transactions.html 串接 | 依賴 Transaction 10 隻 API |
| 🟠 P1 | accounts.html 串接 | 依賴 Account 5 隻 API |
| 🟠 P1 | transfers.html 串接 | 依賴 `POST /api/transfers` + D1 list 決議 |
| 🟠 P1 | categories.html 串接 | 依賴 Category 5 隻 API |
| 🟠 P1 | goals.html 串接 | 依賴 Budget 6 隻 + SavingGoal 7 隻 API |
| 🟠 P1 | analytics.html 串接 | 依賴 Chart 7 隻 API |
| 🟠 P1 | pets.html 完整化 | 依賴 Pet 3 隻 API + D2 / D3 決議 |
| 🟡 P2 | Rive 整合 | 引入 `@rive-app/canvas` runtime + 真實 `.riv` 檔（目前是占位 div） |
| 灰 | D2 互動按鈕 UI | 4 鈕 vs 規格 3 個值，需團隊決議怎麼對應 |
| 灰 | D3 寵物改名 UI | 規格無 update 端點，需決議拿掉 / 補規格 / 包成 interact 子型別 |

---

## 3. API 三向對照（規格 / 前端 / 後端）

> ✅ 已對齊　🟡 部分對齊 / 命名不符　❌ 缺實作　➕ 規格未定義但前端有

| 模組 | 規格端點（4.5）| 前端 [api.js](walletpet-frontend/api.js) | 後端 Controller |
|---|---|---|---|
| **Auth** | `POST /api/auth/login` | ✅ + ➕ `register`（規格未定義，待移除） | ❌ |
| **User** | `GET /api/users/me` | ✅ | ❌ |
| **Account** | `GET/POST /api/accounts`、`GET/PUT/DELETE /api/accounts/{id}` | ✅ | ❌ |
| **Category** | 主 CRUD + `GET /api/categories/available` | 🟡 主 CRUD；缺 `/available` | ❌ |
| **Transaction** | 主 CRUD + 5 隻聚合（`/monthly` `/daily` `/form-meta` `/summary` `/calendar-markers`） | 🟡 主 CRUD；缺其餘 5 隻 | ❌ |
| **Transfer** | `POST /api/transfers`（單一端點） | ✅ `transferApi.create`（commit `ff73c41`） | ❌ |
| **Budget** | 主 CRUD + `GET /api/budgets/{id}/usage` | 🟡 主 CRUD；缺 `/usage` | ❌ |
| **SavingGoal** | 主 CRUD + `/{id}/deposit` + `/{id}/withdraw` + `/{id}/complete` | 🟡 deposit；缺 withdraw / complete | ❌ |
| **Pet** | `/me`、`/interact`、`/reward-by-bookkeeping`（共 3 隻） | ✅ 已對齊（commit `ff73c41`） | 🟡 仍用 `?userId=` + CRUD，違反規格 4.5.2 |
| **Chart** | 7 隻聚合（expense-pie / cashflow-line / monthly-cashflow-line / daily-cashflow-line / summary / monthly-summary / daily-summary） | ✅ `chartApi` 7 隻齊備、走 `/api/charts/*`（commit `ff73c41`） | ❌ |
| **Admin** | `GET/PUT /api/admin/system-data`（需 `role=ADMIN`） | ❌ | ❌ |

---

## 4. 後端目前實作了什麼

依 [walletpet-backend/src/main/java/com/walletpet/](walletpet-backend/src/main/java/com/walletpet/)，**只完成 Pet 模組**：

- [PetController.java](walletpet-backend/src/main/java/com/walletpet/controller/PetController.java)：`/api/pets`（CRUD，與規格不符）
- [PetEventController.java](walletpet-backend/src/main/java/com/walletpet/controller/PetEventController.java)：寵物事件
- Entity / Mapper / Service / Repository：`User`、`Pet`、`PetEvent`
- [CorsConfig.java](walletpet-backend/src/main/java/com/walletpet/config/CorsConfig.java)、[ApiResponse.java](walletpet-backend/src/main/java/com/walletpet/dto/common/ApiResponse.java)、[GlobalExceptionHandler.java](walletpet-backend/src/main/java/com/walletpet/exception/GlobalExceptionHandler.java)

規格 4.5 定義的 11 個模組中，其餘 10 個（Auth / User / Account / Category / Transaction / **Transfer** / Budget / SavingGoal / **Chart** / Admin）**完全沒有 Controller**；Pet 模組雖然存在，但端點與規格不符（見 [§6 P0 #1](#-p0--直接導致打不通)）。

---

## 5. 待組員討論的規格灰色地帶

> 前端 [api.js](walletpet-frontend/api.js) 已於 commit `ff73c41` 對齊規格 4.5 命名與端點，但下列 3 項是規格與現有 UI 衝突、需要團隊決議方向才能繼續推進的問題。

### D1. Transfers 頁的歷史列表怎麼來？

- **背景**：規格 4.5 Transfer 模組只有 `POST /api/transfers`，**沒有 list 端點**；但 [transfers.html](walletpet-frontend/transfers.html) UI 設計需顯示歷史轉帳列表。
- **現況**：[transfers.html:319](walletpet-frontend/transfers.html#L319) 已改用本地 demo；前端不會 404，但歷史是假資料。
- **選項**：
  - (A) 用 `/api/transactions` 加篩選撈轉帳（兩張表 union 邏輯）
  - (B) 跟後端組討論補一隻 `GET /api/transfers`
  - (C) 拿掉 transfers 頁的歷史列表 UI，只保留新增
- **負責**：第 3 組（珮倫）+ 後端架構組

### D2. Pet `interact` 的合法值（規格枚舉 vs 現有按鈕）

- **背景**：規格 4.5 `POST /api/pets/interact` body 為 `{ interactionType: 'CLICK / FEED / PLAY' }`（**3 個值**）；但 [pets.html:231-234](walletpet-frontend/pets.html#L231-L234) 有 4 顆按鈕：`PET / MUSIC / ACHIEVEMENT / CHAT`。
- **現況**：[pets.html:578](walletpet-frontend/pets.html#L578) 已把欄位名改成規格的 `interactionType`，但**值的對應沒處理**；後端如果做 `@Valid` 會擋下來。
- **選項**：
  - (A) 改前端按鈕：4 鈕對應到 `CLICK / FEED / PLAY`（會犧牲 UI 表達力）
  - (B) 跟後端討論放寬枚舉
  - (C) 重新設計 UI：改成 3 鈕並重新命名（摸摸=CLICK、餵食=FEED、玩耍=PLAY）
- **負責**：第 1 組（禹孜）+ 動畫組

### D3. 寵物改名功能要不要？

- **背景**：[pets.html:649-664](walletpet-frontend/pets.html#L649-L664) 有「點擊寵物名稱可改名」UI；但規格 4.5 Pet 模組沒定義 update / rename 端點。
- **現況**：已移除 `petApi.update` 呼叫，改名只更新本地 `state`，重新整理就會掉。
- **選項**：
  - (A) 拿掉前端改名 UI（系統指定預設名 `Mochi`）
  - (B) 後端補一隻 `PUT /api/pets/me`
  - (C) 改名走 `/api/pets/interact` 子型別（不建議，語意混亂）
- **負責**：第 1 組（禹孜）+ 規格擬定者

---

## 6. 詳細落差分析（P0 / P1 / P2）

### 🔴 P0 — 直接導致打不通

1. **Pet 路徑與規格不符**
   規格 4.5：`GET /api/pets/me`；後端 [PetController.java:38-50](walletpet-backend/src/main/java/com/walletpet/controller/PetController.java#L38-L50) 提供 `GET /api/pets?userId=X` 與 `PUT /api/pets/{petId}`，違反規格 4.5.2「不接受前端傳 userId」。
   → 前端 `petApi.me()` 會 404。

2. **缺 `POST /api/pets/interact`** — 前端 `petApi.interact()` 直接 404。

3. **缺 `POST /api/pets/reward-by-bookkeeping`** — 前端 `petApi.rewardByBookkeeping()` 直接 404。

4. **完全沒有 Auth 模組** ⏱️ **約 1–2 天**（與 §2.2 Auth + JWT Filter 同一項）
   規格 4.5 定義 `POST /api/auth/login`，後端沒有對應 Controller、沒有 Spring Security、沒有 JWT 簽發/驗證。
   → 整個登入流程根本跑不起來，`walletpet.jwt` token 也拿不到。
   *備註*：前端 `authApi.register` 與 [login.html:167](walletpet-frontend/login.html#L167) 的 `RegisterView.vue` alert 為**規格未定義**功能，目前不影響登入主流程。

5. **沒有任何 JWT 驗證 Filter（違反規格 4.5.2）**
   規格 4.5.2 明文：「除 `/api/auth/login` 外，所有 API 皆需登入驗證；後端根據 token 取 `currentUserId`，**不接受前端傳入 userId**」。前端塞了 `Authorization: Bearer ...`，但後端不檢查也不驗 token——所有業務資料目前形同公開，多用戶隔離無法成立。

6. **CORS 白名單漏掉常見前端啟動方式**
   [CorsConfig.java:18-21](walletpet-backend/src/main/java/com/walletpet/config/CorsConfig.java#L18-L21) 只放行 `5173 / 4200`。前端是純 HTML，常見會用 VS Code Live Server (`5500`)、`http-server` (`8080`、衝埠！)、或乾脆 `file://` 開——這些全部會被 CORS 擋。

> ✅ 原 P0「規格命名與前端命名不一致（Chart vs Dashboard、Transfer vs AccountTransaction）」已於 commit `ff73c41` 解決，前端 [api.js](walletpet-frontend/api.js) 已對齊規格命名；後端 Chart / Transfer Controller 仍待實作（見 §2.2）。

### 🟠 P1 — 啟動或對接會踩雷

7. **DB schema 必須預先建立**
   [application.properties:11](walletpet-backend/src/main/resources/application.properties#L11) 設 `hbm2ddl.auto=validate`，沒先用 [walletpet.sql](walletpet.sql) 灌資料庫，Spring Boot 啟動就會炸 `SchemaManagementException`。

8. **DB 帳密寫死在 properties**
   [application.properties:5](walletpet-backend/src/main/resources/application.properties#L5) 硬編 `root / 1234`。其他成員 clone 下來幾乎一定要改，且不該進版控（建議改用 `application-local.properties` + `.gitignore`）。

### 🟡 P2 — 不會壞但需要追

9. **後端沒有 `server.port` 設定**
   Spring 預設 8080，前端 `BASE_URL = 'http://localhost:8080'` 剛好能對上；但若有人為了避開 Live Server 改用 `8080` 開前端，就會和後端互相搶埠。

10. **錯誤訊息語系**
    規格 4.5.1 共用回傳格式為 `{ success, message, data, errorCode? }`。後端 [ApiResponse.java](walletpet-backend/src/main/java/com/walletpet/dto/common/ApiResponse.java) 目前**沒有 `errorCode` 欄位**，且 message 中英文混用（`"新增成功"` vs `"Request failed"`）。建議補 `errorCode`，message 統一中文。

11. **主程式類別名品牌大小寫不一致**
    [WalletpetBackendApplication.java](walletpet-backend/src/main/java/com/walletpet/WalletpetBackendApplication.java) 把品牌字寫成 `Walletpet`，但全 codebase 其他地方（前端 `WalletPet.api`、文件、規格）都是 `WalletPet`。
    技術上仍符合 PascalCase 規則（每段字首大寫），但品牌一致性建議改名為 **`WalletPetBackendApplication`**（負責後端啟動模組的組員處理）。
    → 改名時需同步更新：類別名、檔名、[WalletpetBackendApplicationTests.java](walletpet-backend/src/test/java/com/walletpet/WalletpetBackendApplicationTests.java) 與其引用、IDE 執行設定。

---

## 7. SQL Schema 詳細問題（S1–S27）

> **2026-04-26 更新**：SQL 已升級為 **0424 版本**（commit `6aaac95`）。
> ✅ S1 / S18 部分 / S20 / S21 / S22 部分 / S25 已修正；新增 `daily_record_rewards`、`pet_model` 兩張表，以及 `accounts.is_deleted`、`saving_goals.final_account_name / final_amount / status` 等欄位。

### 🔴 P0 — 直接影響登入 / 認證

S1. ~~**`users` 表沒有 `role` 欄位（規格 4.4 / 4.5 明文要求）**~~ ✅ **已修正（0424）**
   現況：[walletpet.sql:367](walletpet.sql#L367) 已加 `role VARCHAR(50) NOT NULL DEFAULT 'USER'`；後端 [User.java](walletpet-backend/src/main/java/com/walletpet/entity/User.java) **仍待補對應欄位**，否則 Admin 模組無法實作。

S2. **`users` 表沒有 `email`，但前端 login 通常會用 email**
   目前只能用 `user_name` 當登入帳號。需確認設計意圖；若要改 email 登入，schema、Entity、Auth DTO 都要一起調整。

S3. **密碼明文儲存（重大資安）**
   [walletpet.sql](walletpet.sql) seed `INSERT INTO users VALUES ('default','system','0000',...)` 直接放明文 `0000`。
   → Auth 模組做之前要先確定改用 BCrypt（Spring Security `PasswordEncoder`），seed 也要重灌成 hash 後的值。

S4. **沒有可登入的 demo user**
   目前只有 `('default', 'system', '0000')` 系統用 user，沒有一般使用者帳號可以測試完整登入流程。

### 🟠 P1 — Entity / 業務邏輯對不上

S5. **PK 型別不一致** ⏱️ **約 1–2 天**（牽動全 Entity / Repository / FK，是 P2 不急但動工成本高）
   - `accounts.account_id` / `account_transactions.account_trans_id` = `INT AUTO_INCREMENT`
   - `pet_events.pet_event_id` = `BIGINT AUTO_INCREMENT`
   - 其餘 (`users / pets / categories / transactions / budget / saving_goals`) 全是 `varchar(50)`
   → 後端 Entity 寫法、Repository 方法簽名都會不一致。建議統一。

S6. **`saving_goals` 已存金額靠帳戶反推，但 SQL 沒約束 `is_saving_account`**
   規格意圖是用綁定的 `account.balance` 反推（一個 saving account = 一個目標）。
   問題：(a) 沒強制 `account.is_saving_account=1`；(b) deposit/withdraw 端點要 atomic 同步更新帳戶餘額。
   → Service 層要做這兩件事；Schema 不需新增欄位但規則必須寫進文件。

S7. **`account_transactions` 跨幣別處理缺欄位**
   `accounts` 應有 `currency_code`（見 S24），但 [walletpet.sql:27-43](walletpet.sql#L27-L43) `account_transactions` 只有單一 `transaction_amount`。
   → 若 `from_account` 跟 `to_account` 幣別不同（TWD ↔ USD），現在無法表達匯率與轉換後金額。

S8. **`transactions` 沒有貨幣欄位**
   Dashboard 聚合多帳戶不同幣別時無法正確加總。建議加 `currency_code` 或在 Service 層強制以 account 的幣別為準。

S9. **`pets` 1:N 但展示中只能 1 隻 — 約束應加在 `is_displayed`**
   規格 4.4：「pets 保存單一使用者的**展示**寵物主狀態」；0424 已加 `model_id` 欄位 ✅（→ 同 user 可能有多筆 pet 紀錄、不同 model）；規格 API `/api/pets/me` 回傳「當前展示中」的那一隻。
   但目前 [walletpet.sql:255-270](walletpet.sql#L255-L270) 仍沒 `UNIQUE(user_id) WHERE is_displayed=1` 約束，後端 [PetController.java:39](walletpet-backend/src/main/java/com/walletpet/controller/PetController.java#L39) `findByUserId` 回 `List`。
   → 正確約束應為「每 user 最多一筆 `is_displayed=1`」。

S10. **`categories` 系統分類掛在 `user_id='default'`**
   [walletpet.sql:152](walletpet.sql#L152) seed 將 4 筆系統分類掛在虛擬 user `'default'`、`is_system=1`。但前端 `categoryApi.list()` 沒帶 user 篩選。
   → 後端 Service 要決定回傳邏輯：「current user 的分類 + `is_system=1` 的系統分類」？

S11. **`categories.icon` 預設值是 `'wait'`**
   seed 把 4 筆 icon 都填 `'wait'`，看起來是 placeholder 給後端日後填真實 icon key。

S12. **`mood` 沒有上下限約束**
   [walletpet.sql:259](walletpet.sql#L259) `mood int NOT NULL` 沒有 `CHECK (mood BETWEEN 0 AND 100)`。
   → 規格 FR-04 要求初始 mood=80，但沒給最大值；後端 interact / reward 邏輯沒寫前，數值可能溢位。
   *備註*：原本 food 欄位已於 commit `b5d6f7d` 砍除（見 S25）。

S20. ~~**`categories` 缺 `is_disable`（規格 4.4 / 4.5 明文要求）**~~ ✅ **已修正（0424）**
   現況：[walletpet.sql:138](walletpet.sql#L138) 已加 `is_disable TINYINT(1) NOT NULL DEFAULT 0`。後端 Category Entity 還沒寫，待補時要含此欄。

S21. ~~**`pets` 缺 `model_id`，且整張 `pet_model` 表不存在**~~ ✅ **已修正（0424）**
   現況：[walletpet.sql:264](walletpet.sql#L264) 已加 `pets.model_id`（FK 到 `pet_model.petmodel_id`），新建 [pet_model 表](walletpet.sql#L231)。後端 `Pet.java` 與 `PetModel` Entity **仍待補對應欄位 / 表**。
   ⚠️ `pet_model` 沒有任何 seed 資料，後端啟動後 `pets.model_id` 也沒得 FK 指向任何記錄。需在 seed 階段先 INSERT 至少一筆預設 model（例如 `(1, 'cat.riv', '預設貓咪')`）。

S22. **`budget` 缺 `budget_name`、`alert_threshold`，仍待補**（0424 已加 saving_goals.status，但 budget 三欄仍缺）
   規格 4.5 `POST /api/budgets` body：`{ budgetName, ..., alertThreshold }`，`GET /api/budgets` 回傳 `budgetName / status / spentAmount / remainingAmount / usageRate`。
   目前 [walletpet.sql:94-110](walletpet.sql#L94-L110) `budget` 表仍缺這 3 欄。
   → 至少要加 `budget_name VARCHAR(50) NOT NULL`、`alert_threshold TINYINT NULL`（百分比 0-100）、`status VARCHAR(20) NULL`（NORMAL/WARNING/OVER_LIMIT/EXPIRED）。

S23. **`pet_events.event_type` 缺枚舉規範**
   規格 4.5 `/api/pets/reward-by-bookkeeping` body 列舉：`TRANSACTION_INCOME / TRANSACTION_EXPENSE / DAILY_BOOKKEEPING_REWARD`；`/api/pets/interact` 還會寫入 `PET_CLICK`。SQL 的 `event_type VARCHAR(50)` 沒有 ENUM 約束。
   → 建議在後端建 `PetEventType` enum 並把可能值寫進 schema 註解。

### 🟡 P2 — 命名 / 規範 / 索引問題

S13. **`budget` 表名單數，其他都複數** — Entity 寫起來 `@Table(name="budget")` 會跟其他模組看起來突兀。

S14. **`budget.target_type` 跟前端常數命名不一致**
   - SQL 欄位：`target_type`
   - 前端常數：`BUDGET_TARGET = { CATEGORY, TOTAL }` ([shared.js:89](walletpet-frontend/shared.js#L89))

S15. **`budget_scope` 沒有 ENUM 約束**
   前端 `BUDGET_SCOPE = { MONTH, WEEK, CUSTOM }`，但 SQL 是 `varchar(20)`。WEEK 在 `start_date / end_date` 怎麼存也未定義。

S16. **缺索引：`account_transactions.transaction_date`**
   transactions 有 `idx_transactions_date` ✓，但 account_transactions 沒索引到 date，dashboard / 報表會慢。

S17. **`categories` 沒有 `UNIQUE(user_id, category_name, category_type)`** — 同一 user 可建立同名分類。

S18. **沒有 soft delete** ⏱️ **約 1 天**（每張表加 `deleted_at` + 全 Repository filter + cascade 規則重審）
   全表都用 `ON DELETE CASCADE / RESTRICT`。一旦刪 user → pet → events → transactions 連環消失，無法恢復。0424 已給 accounts 加 `is_deleted`，但 transactions 等表沒。

S19. **時區處理含糊** ⏱️ **約 1–2 天**（全 codebase 替換 `LocalDateTime` → `OffsetDateTime/Instant` + 所有 DTO 格式 + 前端顯示）
   [walletpet.sql:14](walletpet.sql#L14) `SET TIME_ZONE='+00:00'`，但所有 `created_at` 都是 `datetime`（無時區），後端用 `LocalDateTime`。跨時區部署一定踩雷。

### 🆕 0424 版本特有問題

S24. **`accounts.currency_code` 被砍掉（疑似 regression）**
   舊版 SQL 有 `currency_code VARCHAR(10) NOT NULL DEFAULT 'TWD'`，0424 版本 [walletpet.sql:62-75](walletpet.sql#L62-L75) **移除此欄位**。
   但規格 4.4 / 4.5 仍要求：規格 4.4 schema 明文 `accounts: ..., balance, **currency_code**, is_liability, ...`；規格 4.5 `GET /api/accounts` 回傳示例含 `currencyCode: "TWD"`。
   → 需團隊決定：(A) 加回該欄位（保留多幣別能力）；(B) 確認砍掉並同步更新規格與前端常數 [shared.js:91](walletpet-frontend/shared.js#L91) `CURRENCY_DEFAULT: 'TWD'`。
   **負責人**：第 3 組（珮倫）+ 規格擬定者。

S25. ~~**`pets.food` 還沒砍乾淨（食物決議只執行半套）**~~ ✅ **已修正（commit `b5d6f7d`）**
   貫徹專案「砍 food」決議，已同時清掉：
   - [walletpet.sql:260](walletpet.sql) `pets.food` 欄位 ✓
   - 後端 7 處 food / foodDelta：Pet.java / PetEvent.java / 5 DTO ✓
   - PetMapper / PetEventMapper / PetServiceImpl 內所有 food 引用 ✓
   ⚠️ **規格 4.4 / 4.5 仍提到 `food`**（如 `/api/pets/me` 回傳值含 `food: 10`）需要同步更新規格文件。

S26. **`saving_goals.final_account_name` 是字串而非 FK（資料完整性疑慮）**
   0424 版新增 `final_account_name VARCHAR(100)` + `final_amount DECIMAL(12,2)` 欄位，用於 UC-26「終結存款目標」流程保存最終帳戶資訊。
   但用「字串」存帳戶名稱、不是 FK 到 `accounts.account_id`，未來若 user 改名帳戶就會對不上歷史記錄。
   → 設計取捨：是否改成 FK？若刻意要「快照當下名稱」，欄位命名應改 `final_account_name_snapshot` 表達意圖。

S27. **`daily_record_rewards` 缺 enum 約束**
   0424 版新增 [daily_record_rewards 表](walletpet.sql#L163-L180)（streak / qualified / mood_delta），對齊 FR-04 + 流程 D-1 是好設計。
   但 `reward_type VARCHAR(50)` 沒 enum 約束（業務上應限定如 `STREAK_3 / STREAK_7 / DAILY_FIRST` 等）。
   → enum 規範需後端 Service 層加 `DailyRewardType` 常數；schema 可加註解。

### 🧱 後端 Entity 缺漏

對應 SQL 表，但後端 [entity/](walletpet-backend/src/main/java/com/walletpet/entity/) 目前只有 `User / Pet / PetEvent`，下列 Entity 全部沒寫：

- [ ] `Account`（accounts）
- [ ] `AccountTransaction`（account_transactions）
- [ ] `Category`（categories）— 注意要含 `is_disable`
- [ ] `Transaction`（transactions）
- [ ] `Budget`（budget）— 注意要含 `budget_name / alert_threshold`（待 S22 SQL 同步）
- [ ] `SavingGoal`（saving_goals）— 含 0424 新增的 `status / final_*`
- [ ] `PetModel`（pet_model — 0424 新表）
- [ ] `DailyRecordReward`（daily_record_rewards — 0424 新表）

由於 `application.properties` 設 `hbm2ddl.auto=validate`，**只要任何 Entity 跟 SQL 對不上（少一欄、命名不同、型別不符）後端就會啟動失敗**——這是接下來補模組時最容易踩的雷。

---

## 8. 串接順序建議

1. **SQL 補欄位**：S22 budget 三欄、S24 currency_code 決議、S21 pet_model seed
2. **後端：Auth + JWT Filter + User /me** → 前端登入流程能跑（同時上 BCrypt）
3. **後端：Pet 改 `/me` + interact + reward-by-bookkeeping**，砍掉 `?userId=` CRUD
4. **後端：Category（含 `/available`）→ Transaction（含 `/monthly` `/daily` 等聚合）→ Account**
5. **後端：Budget（含 `/usage`）/ SavingGoal（含 `/withdraw` `/complete`）/ Transfer（單一 POST）**
6. **後端：Chart（7 隻）/ Admin（2 隻）**
7. **前端**：~~api.js 重新命名~~（已完成 ✅ commit `ff73c41`）→ 401 攔截、BASE_URL 可設定、處理 §5 灰色地帶

---

## 9. 本機啟動

```bash
# 1. 建 DB schema
mysql -u root -p walletpet < walletpet.sql

# 2. 後端
cd walletpet-backend
./mvnw spring-boot:run    # http://localhost:8080

# 3. 前端（任一方式）
# A. VS Code Live Server 開 walletpet-frontend/login.html  (port 5500，需先把 5500 加進 CORS)
# B. 用 Python 簡易伺服器
cd walletpet-frontend && python -m http.server 5173
```

> 📱 **進階（本機跨裝置 + HTTPS、PWA「加到主畫面」讓 Android / iOS 像 App 執行）→ 見 [§10 跨平台執行方案](#10-跨平台執行方案)**

---

## 10. 跨平台執行方案

> 📍 **本章 = [§11 部署策略](#11-部署策略選擇) B 選項（LAN 主持）的完整實作章節**。建議先看 [§11](#11-部署策略選擇) 比較 A / B / C 三條策略再回來；若選 A（雲端）或 C（Docker），本章的 §10.2 / §10.3 / §10.6 仍然適用。
>
> **目標**：電腦以網頁 / 桌面軟體形式執行，Android / iOS 以「像 App」的形式執行，**全部在本地 LAN 內**（不上 App Store / Play Store / 公網）。
>
> **本團隊技術棧前提**：HTML / CSS / JS（無打包工具）、Spring Boot 3.5（Java 17）、MySQL、VS Code、Maven、`keytool`（JDK 內建）、`python -m http.server`。
>
> **本章節僅保留與上述技術棧相容的方案**，已剔除需要 Node.js / Android Studio / Rust / Xcode / Apple Developer 帳號的選項（Capacitor、Electron、Tauri、真 IPA）—— 那些工具鏈對純新手團隊的學習成本遠高於 PWA 路線本身。
>
> **工時欄位**有兩欄：
> - **無 AI**：一般工程師不使用 AI 輔助，從零查資料、實作、踩雷、實機驗證的合理估時。
> - **有 Claude**：由熟練 Claude Code 操作者執行，AI 直接產出 code / config，剩下人類仍要做的部分（指令執行、實機驗證、設計母圖等）。

---

### 10.1 三平台對應方案速查

| 平台 | 推薦方案 | 為何符合本團隊 |
|---|---|---|
| 💻 電腦 | 瀏覽器開網頁；想要視窗化 → Chrome 右上角 ⋮ → 「投放、儲存、共用」→「安裝 WalletPet」（或舊版「建立捷徑」勾「以視窗開啟」） | 0 額外技術，所有人都會 |
| 🤖 Android | **PWA「加到主畫面」**（Chrome 自帶提示） | 純 HTML/JS/CSS + 多兩個檔（`manifest.json`、`service-worker.js`） |
| 🍎 iOS | **PWA「加到主畫面」**（Safari → 分享 → 加入主畫面） | 同上，唯一額外要求是 HTTPS（見 §10.4） |

→ **走 PWA 路線完全在本團隊技術棧內**，只是多寫幾個靜態檔。

---

### 10.2 LAN 連線必補（三平台都要做）

| # | 問題 | 現況位置 | 改法 | 無 AI | 有 Claude |
|---|---|---|---|---|---|
| L1 | 前端 `BASE_URL` 寫死 `localhost` | [shared.js:102](walletpet-frontend/shared.js#L102) | 改 `` `http://${location.hostname}:8080` ``，PC / 手機自動對應 | 30 分 | 1 分 |
| L2 | CORS 只放行 `localhost:5173 / 4200` | [CorsConfig.java:17-21](walletpet-backend/src/main/java/com/walletpet/config/CorsConfig.java#L17-L21) | 改 `allowedOriginPatterns("*")`（dev 期間） | 30 分 | 2 分 |
| L3 | Controller 硬編 `@CrossOrigin` 覆蓋全域 | [PetController.java:24](walletpet-backend/src/main/java/com/walletpet/controller/PetController.java#L24)、[PetEventController.java:22](walletpet-backend/src/main/java/com/walletpet/controller/PetEventController.java#L22) | 直接刪掉這兩行 | 10 分 | 1 分 |
| L4 | 沒有靜態 dev server（`file://` 手機開不了） | — | **延用 §9 既有方式**：`python -m http.server 5500 --bind 0.0.0.0`；或 VS Code Live Server 把 `host` 設 `0.0.0.0` | 30 分 | 5 分 |
| L5 | Windows 防火牆擋外部進站 | — | 控制台「Windows Defender 防火牆 → 進階設定 → 輸入規則」加 TCP 8080 + 5500；或用 PowerShell `New-NetFirewallRule` | 30 分 | 10 分（指令可生但仍需手動操作 OS） |
| L6 | 同網段手機驗證 + debug | — | `ipconfig` 取 IPv4，手機開 `http://192.168.x.x:5500/login.html` | 30–60 分 | 30 分（實機操作必須人做） |

**§10.2 合計：無 AI 約 2–3 小時 / 有 Claude 約 50 分鐘**

> ⚠️ 若採用 §10.4 A 方案（Spring Boot 接管前端），**L2 / L3 / L4 全部可省**（同 origin 不需 CORS、Spring Boot 自帶靜態檔伺服器）。

---

### 10.3 PWA 必備檔案（讓 Android / iOS 能「加到主畫面 = 像 App」）

| # | 缺漏項 | 備註 | 無 AI | 有 Claude |
|---|---|---|---|---|
| P1 | `manifest.json` | `name`、`display:"standalone"`、`start_url`、`theme_color`、`background_color`、icons 陣列 | 30–60 分 | 5 分（Claude 直接寫） |
| P2 | App icons：`icon-192.png` / `icon-512.png` / `apple-touch-icon-180.png` | 不需 Node.js 工具，可用線上 [realfavicongenerator.net](https://realfavicongenerator.net) 從一張母圖產全套 | 1–2 小時 | 30 分（瓶頸是設計母圖） |
| P3 | `service-worker.js` 空殼 | iOS **沒有 SW 不會視為獨立 app**（會頂著 Safari UI 跑），即使不做離線快取也必備 | 30–60 分 | 5 分（Claude 寫好骨架） |
| P4 | 9 個 HTML 各加 5 行 meta（`manifest`、`apple-touch-icon`、`apple-mobile-web-app-capable`、`status-bar-style`、`theme-color`） | 全頁同步，漏一頁就漏一頁的 standalone 樣式 | 30–45 分 | 5 分（Claude 一次改完） |
| P5 | `shared.js` 加 SW 註冊 | `navigator.serviceWorker.register('./service-worker.js')` | 30 分 | 3 分 |

**§10.3 合計：無 AI 約 3–5 小時 / 有 Claude 約 50 分鐘**（瓶頸是 P2 設計母圖）

---

### 10.4 HTTPS — iOS 安裝 PWA 的硬性要求

iOS Safari 安裝 PWA **必須 HTTPS**（`localhost` 例外，但手機連到 `192.168.x.x` **不算例外**）。Android Chrome 寬鬆些但 PWA 安裝按鈕仍需 HTTPS。

**在本團隊技術棧內最自然的做法是 A 方案（Spring Boot 接管前端 + SSL）**——只用 Spring Boot + JDK 內建的 `keytool`，不引入新工具鏈。

| 方案 | 描述 | 是否在本團隊技術棧內 | 無 AI | 有 Claude |
|---|---|---|---|---|
| **A. Spring Boot 接管前端 + SSL**（強烈推薦） | 1）`application.properties` 加 `spring.web.resources.static-locations`<br>2）`keytool -genkeypair` 產 keystore（JDK 內建）<br>3）`server.ssl.*` 開 8443<br>4）前端 BASE_URL 改 `` `${location.origin}` ``（同 origin 連 §10.2 L2/L3/L4 全省）<br>📋 **完整 7 步實作計畫見 [§10.9](#109-104-a-方案實作計畫下一步預定執行)** | ✅ 全部都是 Spring Boot 設定 + JDK 內建工具 | 3–5 小時 | 30–60 分 |
| **B. mkcert 自簽憑證 + 外部前端 server** | 多裝 mkcert CLI（Windows 用 Chocolatey 或下載 exe），前端 server 也要支援 HTTPS | ⚠️ 需學新工具，不推薦本團隊 | 2–4 小時 | 1–2 小時 |
| **C. ngrok 內網穿透** | 拿外部 HTTPS URL 給手機 | ❌ 違反「純本地」（走公網） | 1 小時 | 30 分 |
| **D. 暫不上 HTTPS，先做電腦 + Android** | Android Chrome 在 HTTP LAN 仍可加到主畫面（無 install prompt 但 menu 可選），iOS 直接放棄 | ✅ 0 額外工，但 iOS 退化成普通網頁 | 0 | 0 |

> 🔑 **採 A 方案（Spring Boot 接管前端 + SSL）後架構簡化**：Spring Boot 一個服務同時吐前端 + API，全部走 `https://192.168.x.x:8443/...`，CORS 不再是問題。手機要嘛接受瀏覽器的「不安全憑證」警告，要嘛把 keystore 對應的 cert 灌進手機系統信任清單。

---

### 10.5 iOS PWA 多 HTML 跳轉地雷

目前前端是 9 個獨立 HTML（`login` → `dashboard` → …），**iOS PWA 在 `display:standalone` 模式下，跨 HTML 用 `location.href` 跳轉有機會被踢出 Safari**（Apple 多年未修的 bug）。三條解法：

| 方案 | 描述 | 無 AI | 有 Claude |
|---|---|---|---|
| **接受 + 實機驗證**（推薦先試） | 跑一輪 9 頁所有導航；多數 iOS 17+ 已改善，可能不需處理 | 30 分 | 30 分 |
| **改 hash 路由** | 用 `shared.js` 攔截 `<a>` 與 `location.href`，改用 `#/dashboard`；9 頁仍各自獨立但不離開殼 | 1–2 天 | 2–4 小時 |
| **改 SPA** | 9 頁合併到單一 HTML，引入 router | 3–5 天 | 1–2 天（等同重寫前端） |

---

### 10.6 跨平台佈局微調

| 項目 | 描述 | 無 AI | 有 Claude |
|---|---|---|---|
| iOS Safari `100vh` bug | dashboard 用 flex 撐滿，iOS 網址列會吃掉視窗高度 → 改用 `100dvh` 或 JS 量測 | 30–60 分 | 10 分 |
| 安全區（瀏海 / Dynamic Island） | `viewport-fit=cover` 已加 ✅，但 CSS 沒用 `env(safe-area-inset-*)`，需逐頁補 padding | 30–60 分 | 15 分 |
| 觸控按鈕加大（phone-specific） | 現有 `@media (max-width: 899px)` 是平板等級，360 px 寬手機按鈕偏小 | 2–4 小時（9 頁） | 30–60 分 |
| iOS PWA splash 啟動圖 | 不給就是白畫面；`apple-touch-startup-image` 各裝置尺寸不同，[realfavicongenerator.net](https://realfavicongenerator.net) 可一次產全套 | 1–2 小時 | 30 分（仍要產圖） |
| status-bar 顏色 | `apple-mobile-web-app-status-bar-style`（`default` / `black` / `black-translucent`）三選一 + 實機調 | 30 分 | 20 分 |
| Google Fonts 離線備援 | [dashboard.html:8-9](walletpet-frontend/dashboard.html#L8-L9) 仍走 CDN，純內網無法載入 → 下載 woff2 自帶 | 30–60 分 | 15 分 |
| Rive 動畫資產 | 確認 `.riv` 走本地路徑而非 CDN，否則純內網無法載入 | 30 分 | 10 分 |

**§10.6 合計：無 AI 約 5–10 小時 / 有 Claude 約 2–3 小時**

---

### 10.7 完整 PWA 最小工清單（三平台跑通）

| # | 步驟 | 無 AI | 有 Claude |
|---|---|---|---|
| 1 | §10.4 採 **A 方案 Spring Boot 接管前端 + SSL**（同時搞定 §10.2 L1/L2/L3/L4） | 3–5 小時 | 30–60 分 |
| 2 | §10.2 剩餘項：L5 防火牆 + L6 手機驗證 | 1 小時 | 30–40 分 |
| 3 | §10.3 PWA 檔案（P1–P5） | 3–5 小時 | 50 分 |
| 4 | §10.6 佈局微調 | 5–10 小時 | 2–3 小時 |
| 5 | §10.5 跨 HTML 實機驗證（先選「接受」） | 30 分 | 30 分 |
| 6 | 三平台實機驗收（PC Chrome、Android Chrome 加到主畫面、iOS Safari 加到主畫面） | 1–2 小時 | 1–2 小時 |
| **合計** | | **約 14–24 小時 ≒ 2–3 工作天** | **約 5–8 小時 ≒ 1 工作天** |

> ⚠️ 本表不含 [§2 待修正事項總覽](#2-待修正事項總覽) 的 P0（Auth/JWT、密碼 BCrypt、Pet 改造、各模組 CRUD）—— 那些是「任何平台都跑不起來」的問題，必須先解決。

---

### 10.8 補充選項（電腦端「軟體化」零成本招式）

| 方案 | 描述 | 無 AI | 有 Claude |
|---|---|---|---|
| **Chrome「安裝為應用程式」** | 開啟 PWA URL → 網址列右側「安裝」圖示一按；Edge 同款 | 1 分 | 1 分 |
| **Chrome「建立捷徑」勾「以視窗開啟」** | 較舊版 Chrome 沒有 PWA install 按鈕時的後備；產生獨立 taskbar 圖示 | 5 分 | 5 分 |

> 已**不採用** Capacitor / Electron / Tauri / 真 IPA —— 那些超出本團隊技術棧（要 Node.js + Android Studio / Rust / Mac + Xcode），即使有 Claude 輔助也會卡在環境裝設、SDK 配對、簽名授權等「Claude 看不到的本地步驟」。期末展示如需 .exe 等實體檔再評估。

---

### 10.9 §10.4 A 方案實作計畫（下一步預定執行）

> 🏷️ **A 方案 = Spring Boot 接管前端 + SSL**（與 [§11.1 B. LAN 主持](#111-三項主要選擇比較) 是同一條路徑，本節是其完整實作）。
>
> **狀態**：尚未動工，等團隊確認後執行。本節記錄完整步驟與反悔成本，方便任何人接手。
>
> **目標**：把 Spring Boot 從「只吐 API」升級成「同時吐前端 + API + HTTPS」，三平台用單一 URL `https://192.168.x.x:8443/login.html` 全搞定。

#### Step 1 — Spring Boot 加靜態檔案路徑

[application.properties](walletpet-backend/src/main/resources/application.properties) 加：

```properties
spring.web.resources.static-locations=file:../walletpet-frontend/,classpath:/static/
```

- `file:../walletpet-frontend/` 是相對於 **Spring Boot 啟動時 working directory**；目前 §9 流程是 `cd walletpet-backend && ./mvnw spring-boot:run`，故 `..` 指回專案根
- 保留 `classpath:/static/` fallback，以防之後想把前端打包進 jar
- 驗證：啟動後 `http://localhost:8080/login.html` 應直接顯示登入頁

#### Step 2 — 用 keytool 產 keystore（JDK 內建，不裝任何工具）

```bash
keytool -genkeypair -alias walletpet -keyalg RSA -keysize 2048 \
  -storetype PKCS12 \
  -keystore walletpet-backend/src/main/resources/walletpet.p12 \
  -validity 365 \
  -dname "CN=walletpet, OU=dev, O=walletpet, L=Taipei, ST=Taipei, C=TW" \
  -storepass walletpet-dev -keypass walletpet-dev
```

- `walletpet-dev` 是 dev 密碼，**僅本機使用**
- ⚠️ `.p12` 含密鑰，**必須加進 [.gitignore](.gitignore)**（不能 push）→ 每位組員 clone 後要自己跑這條指令一次
- 建議寫成 `walletpet-backend/setup-keystore.bat` / `.sh` 讓組員一鍵跑

#### Step 3 — application.properties 開 SSL

```properties
server.port=8443
server.ssl.enabled=true
server.ssl.key-store=classpath:walletpet.p12
server.ssl.key-store-type=PKCS12
server.ssl.key-store-password=walletpet-dev
server.ssl.key-alias=walletpet
```

> 建議拆 `application-local.properties` 把 `key-store-password` 抽出去，避免 dev 密碼進版控；同 [§6 P1 #8](#-p1--啟動或對接會踩雷) 建議的 DB 帳密處理方式。

#### Step 4 — 前端 BASE_URL 改為同 origin

[shared.js:102](walletpet-frontend/shared.js#L102) 改：

```js
const BASE_URL = window.location.origin;
// PC 走 https://localhost:8443，手機走 https://192.168.x.x:8443，全自動
```

順手清掉 [shared.js:122](walletpet-frontend/shared.js#L122) 「無法連接後端 http://localhost:8080」的 hard-coded 訊息。

#### Step 5 — CORS 縮減（同 origin 已不需要）

- [CorsConfig.java](walletpet-backend/src/main/java/com/walletpet/config/CorsConfig.java)：可整個刪除或保留（同 origin 不會觸發）
- [PetController.java:24](walletpet-backend/src/main/java/com/walletpet/controller/PetController.java#L24) / [PetEventController.java:22](walletpet-backend/src/main/java/com/walletpet/controller/PetEventController.java#L22) 的 `@CrossOrigin` **必須刪掉**，否則同 origin 仍會報 warning

#### Step 6 — Windows 防火牆 + 手機驗證

1. 控制台「Windows Defender 防火牆 → 進階設定 → 輸入規則」加 TCP `8443`（取代原本的 8080 / 5500）
2. `ipconfig` 取 IPv4
3. PC：`https://localhost:8443/login.html`（瀏覽器跳憑證警告 → 接受）
4. Android Chrome / iOS Safari：`https://192.168.x.x:8443/login.html`（同樣跳警告 → 接受）
5. （加分項）把 cert 匯出 `.cer` 灌進手機「設定 → 一般 → 描述檔」並啟用「完整信任」，即可消除警告

#### Step 7 — 同步更新 §9 本機啟動

採用本方案後 §9 的流程簡化為：

```
1. mysql -u root -p walletpet < walletpet.sql
2. 第一次：執行 setup-keystore 產 walletpet.p12
3. cd walletpet-backend && ./mvnw spring-boot:run
4. 開 https://localhost:8443/login.html
```

不再需要 Live Server / Python http.server。需同步改本 README §9。

#### 預估工時

| 步驟 | 無 AI | 有 Claude |
|---|---|---|
| 1. 靜態檔案路徑（properties 一行） | 30 分 | 3 分 |
| 2. keytool 產 keystore + 寫 setup script | 30–60 分 | 10 分（指令必須人跑） |
| 3. SSL properties | 30 分 | 3 分 |
| 4. 前端 BASE_URL | 30 分 | 1 分 |
| 5. CORS 縮減 | 15 分 | 2 分 |
| 6. 防火牆 + 手機驗證 | 1 小時 | 30 分（實機操作必須人做） |
| 7. §9 README 同步 | 15 分 | 5 分 |
| **合計** | **約 3–4 小時** | **約 55 分** |

#### 反悔成本（完全可逆）

| 步驟 | 反悔方式 | 工時 |
|---|---|---|
| 1, 3, 5, 7 | 註解 / 還原 properties + Java 註解 | < 5 分 |
| 2 | 砍 `walletpet.p12`，停用 SSL 設定 | < 1 分 |
| 4 | `shared.js` 改回硬編 `http://localhost:8080` | < 1 分 |

→ **任一階段都能回退**，建議放心試做。

#### 動工前 Pre-checks

- [ ] [§2 P0](#2-待修正事項總覽) 進度確認（本方案本身不依賴 P0 完成，但完整測試流程仍需先有登入）
- [ ] 跟所有組員溝通：啟動命令仍從 [walletpet-backend/](walletpet-backend/) 內跑
- [ ] `.gitignore` 加 `*.p12`、`application-local.properties`
- [ ] 把 keystore 產生指令寫成 `walletpet-backend/setup-keystore.sh` / `.bat` 讓組員一鍵跑
- [ ] 確認所有組員 Spring Boot 啟動正常（沒有 schema validate 失敗），避免本方案上去後混淆「是 SSL 壞還是原本就壞」

---

## 11. 部署策略選擇

> **§11.1 ~ §11.3 主推 A（雲端） / B（LAN） / C（Docker）**：必須完整展示課程核心 = **Spring Boot + MySQL + JPA + Hibernate + Maven**。
> **已剔除（從 A / B / C 主軸）**：H2 嵌入式 DB（失去 MySQL）、MariaDB4j 嵌入（非真 MySQL 且不穩）。
> **[§11.4](#114-純前端緊急-fallbackoption-d) 純前端 fallback（D）**：實作已分流到 [`frontend-only` branch](https://github.com/WalletMeowStudio/walletpet/tree/frontend-only) 維護，不展示後端課程 —— 列為緊急方案 / 離線版副產品。

### 11.1 三項主要選擇比較

| 選擇 | 用到的技術（課程內） | 需用到的額外技術 | 優缺點 |
|---|---|---|---|
| **A. Railway 雲端部署** | Spring Boot 3.5<br>MySQL（Railway plugin）<br>JPA / Hibernate<br>Maven<br>application.properties | Git / GitHub workflow<br>環境變數抽取（`${DB_URL}` 等）<br>application-prod profile<br>Dockerfile（選用）<br>雲端 log 讀取 | ✅ 三平台一個 HTTPS URL，自動 SSL<br>✅ **履歷大加分**（雲端部署是面試必考）<br>✅ 評審零操作門檻，開連結即可<br>✅ 評審 / 老師可隨時自己開來看<br>⚠️ 第一次 deploy 看 log 有學習曲線<br>⚠️ Demo 後免費額度會耗盡，服務會停<br>🔴 Demo 場地必須有網路 |
| **B. LAN 主持**（[§10](#10-跨平台執行方案) 已規劃） | Spring Boot 3.5<br>MySQL（本機）<br>JPA / Hibernate<br>Maven<br>keytool（JDK 17 內建） | （幾乎沒有）<br>Windows 防火牆設定<br>自簽憑證概念 | ✅ 0 雲端依賴 + 0 月費，**永久免費**<br>✅ [§10.9](#109-104-a-方案實作計畫下一步預定執行) 已寫好完整 SOP，可立即執行<br>✅ 開發期就能用，無 dev / demo 環境差異<br>✅ 5 分鐘可切回 localhost（失敗無痛）<br>⚠️ 手機要接受不安全憑證警告（或灌根憑證）<br>🔴 評審 / 老師要連同一個 WiFi<br>🔴 PC 主機關機 = 服務停<br>🔴 履歷展示「會 deploy」較弱 |
| **C. Docker Compose 一鍵起整套** | Spring Boot 3.5<br>MySQL（容器）<br>JPA / Hibernate<br>Maven | **Docker**（全新技術）<br>**Docker Compose**（全新技術）<br>Dockerfile + docker-compose.yml<br>容器網路概念 | ✅ 評審 `docker compose up` 一行起整套<br>✅ 環境零汙染（不用裝 MySQL service）<br>✅ Docker 履歷加分（業界普及度高）<br>✅ 也可疊加在 A 或 B 上（雲端 / LAN 都能跑 Docker）<br>⚠️ Docker 學習成本約 4–8 小時（無 AI）/ 1–2 小時（有 Claude）<br>⚠️ Image 體積約 400–600 MB<br>🔴 評審要先裝 Docker Desktop（Windows 需 WSL2）<br>🔴 **本身不解決手機跨裝置問題**（仍要疊 A 或 B） |

### 11.2 重要補充

> 🔗 **C 是「包裝層」不是獨立的部署策略** —— Docker Compose 解決的是「環境一致性 / 安裝便利性」，不解決「如何讓手機連得到」。實務上會是：
> - **A + C**：把 Spring Boot + MySQL 用 Docker Compose 包好，部署到 Railway（Railway 直接吃 Dockerfile，部署更穩）
> - **B + C**：本地用 Docker Compose 跑，然後其他人連同網段（等於 §10 LAN 但用 Docker 起服務）

> 📱 **三個選擇都需要疊加 [§10.3](#103-pwa-必備檔案讓-android--ios-能加到主畫面--像-app) PWA 套件** —— 這是讓 Android / iOS「像 App」執行的必備條件，跟部署策略無關。

### 11.3 建議組合（職訓班場景）

| 階段 | 推薦組合 | 理由 |
|---|---|---|
| 🛠️ 開發期 | **B（LAN 主持）** | §10.9 SOP 已備，工時最低，組員開發測試最方便 |
| 🎓 Demo 前一週 | **A（Railway 雲端）** + **C（Dockerfile）** | 雲端部署是履歷加分主菜，Dockerfile 是配菜 |
| 🎤 Demo 當天 | 主秀 **A（雲端 URL）**，fallback 切回 **B（LAN）** | 雙重保險，雲端壞了 5 分鐘切換 |

→ 這樣三項技術全展示：**B 證明「會本機 + HTTPS」、A 證明「會雲端部署」、C 證明「會容器化」**。

### 11.4 純前端緊急 fallback（Option D）

> 🌿 **實作已分流到獨立 branch [`frontend-only`](https://github.com/WalletMeowStudio/walletpet/tree/frontend-only)**，不在 main 主開發路線上 —— main README 僅保留與本團隊技術棧（Spring Boot + MySQL + JPA + Maven）一致的方案討論。
>
> Option D 不對接 Spring Boot 後端，所有資料存於瀏覽器 localStorage / IndexedDB —— 完全失去課程後端展示，僅作**緊急 fallback** 或「離線版副產品」保留。
>
> 主開發請維持 [§11.1 ~ §11.3](#111-三項主要選擇比較) 的 A（雲端）/ B（LAN）/ C（Docker）三選一。

#### 何時切到 `frontend-only` branch

| 情境 | 動作 |
|---|---|
| 開發期，後端進度正常 | ❌ 不需要碰 |
| Demo 前一週，後端 P0 進度 < 50% | ⚠️ 二擇：抓緊做完 / 切 `frontend-only` 做副本 |
| Demo 前 24 小時，後端炸了 | ✅ 緊急切 `frontend-only` 頂上去 |
| 想保留「離線版 / 個人版」副產品 | ✅ 雙軌維護（main backend 版 + frontend-only 離線版並存） |

#### 切換指令

```bash
git checkout frontend-only      # 切到純前端版
git checkout main               # 切回主版（後端 + Spring Boot）
```

→ **詳細實作 SOP**（重寫 api.js、儲存方案 D1 localStorage / D2 IndexedDB、工時、Netlify 部署、Dexie.js adapter 進階構想）：在 [`frontend-only` branch 的 README.md](https://github.com/WalletMeowStudio/walletpet/blob/frontend-only/README.md) 中維護。

---

### 11.5 duo branch（未來規劃）

> 🌱 **第三條長期分支：`duo`** —— 將 `main` 後端串接版前端與 `frontend-only` 純瀏覽器版前端**整合成同一份程式碼**，部署時用 flag 切換雙模式。
>
> **目前狀態**：分支已建立，僅含規劃 README，尚未開始實作。等 `main` 後端穩定 + `frontend-only` D2 SOP 完成後再動工。

#### 為什麼要有 duo branch？

| 目的 | 說明 |
|---|---|
| 🎓 demo 故事性 | 「同一份前端，可以掛 Spring Boot 也可以掛瀏覽器 IndexedDB」展示**架構彈性** |
| 🔌 部署彈性 | 雲端展示走 Spring Boot 模式、純前端 fallback 走 IndexedDB 模式，**單一程式碼維護** |
| 🛡️ 容災備援 | demo 當天若雲端 / 後端炸鍋，可立刻切到 local 模式繼續展示 |
| 📚 履歷加分 | Adapter pattern + 環境 flag 切換實作，是面試常問的「設計模式應用」實例 |

#### 三條 branch 的角色（再次強調獨立性）

| branch | 用途 | 與其他 branch 的關係 |
|---|---|---|
| `main` ⭐ 主開發 | 後端串接版 | `duo` 以此為基底新開，但**不合併回來** |
| `frontend-only` | 純前端版 | `duo` 從此**人工複製** LocalApiAdapter，但**不合併** |
| `duo` | 雙模式整合版 | 由開發者**人工同步** main / frontend-only 的重要修正 |

#### 啟動條件與時機

採用 duo 之前，**main 與 frontend-only 必須先各自完成自己的實作**（否則沒東西可整合）：

| 前置 | 進度要求 |
|---|---|
| `main` | 完成 [§2 P0](#2-待修正事項總覽)（Auth / JWT、Pet 改造、各模組 CRUD） + 對應前端 fetch 版 `api.js` |
| `frontend-only` | 完成 [§5.4 D2 SOP](https://github.com/WalletMeowStudio/walletpet/blob/frontend-only/README.md#54-d2-indexeddb--dexiejs-進階) IndexedDB + Dexie 版 |

→ 太早整合會多次重工，建議等「兩條 branch 都跑過至少一輪驗證」後再開 duo。

#### 詳細實作 SOP

完整啟動步驟、Adapter pattern 實作、部署矩陣、人工同步維護策略，請見 [`duo` branch 的 README.md](https://github.com/WalletMeowStudio/walletpet/blob/duo/README.md)。
