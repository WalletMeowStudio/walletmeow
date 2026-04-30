# WalletPet Frontend

WalletPet 前端 UI 骨架（純 HTML / CSS / JS，無建置流程）。

> 📘 **架構總覽 / API 對接狀態 / 規格落差 / 待組員討論項目**
> 全部集中在 [專案主 README](../README.md)。本檔只負責「**怎麼把頁面跑起來看畫面**」。

> ⚠️ **目前狀態：UI 預覽版，API 未串接**
> 後端目前只實作了 Pet 模組，且端點與規格不符。所有 API 呼叫都會走到 catch 區塊使用本地 demo 資料，不會 crash 但也不會有真實後端互動。

## 快速預覽

### 方法 1：直接打開 HTML（最快）
雙擊任何一個 `.html`（建議從 `dashboard.html` 開始）。`login.html` 的「註冊」鈕目前只是 `alert` 占位，不會打 API；只有「登入」鈕真的會送 `POST /api/auth/login`，後端沒實作所以會失敗。

注意：`file://` 協議下 `fetch` 會被 CORS 擋（後端白名單只有 5173/4200，沒有 `null` origin），console 會看到網路錯誤，但畫面 UI 仍然可看。

### 方法 2：本地起一個簡單 server（推薦）
```bash
# Python 3
python -m http.server 5173

# 或 Node
npx serve -l 5173
```
然後打開 http://localhost:5173/dashboard.html

用 `:5173` 是因為後端 CORS 已經把這個 port 加白名單，等 API 接通就能直接打通。

### 方法 3：GitHub Pages
等 repo owner 開啟 GitHub Pages 後，可直接用網址看，不用 clone。

## 檔案結構

| 檔案 | 用途 |
|---|---|
| `login.html` | 登入頁（登入按鈕會送 `POST /api/auth/login`，後端未實作；註冊鈕為 alert 占位） |
| `dashboard.html` | 主儀表板 |
| `transactions.html` | 交易紀錄 |
| `accounts.html` | 帳戶管理 |
| `transfers.html` | 轉帳 |
| `categories.html` | 分類管理 |
| `goals.html` | 預算與存錢目標 |
| `pets.html` | 寵物互動 |
| `analytics.html` | 數據分析 |
| `shared.css` | 共用樣式 |
| `shared.js` | 共用工具：API wrapper、formatters、toolbar、tweaks 面板 |
| `api.js` | 各模組 API 呼叫定義（已對齊規格 4.5；詳見主 README §2） |

## 想知道更多？

- **整體架構、後端模組現況、SQL 缺口** → [主 README §1-§5](../README.md)
- **後端 / 前端 / SQL 待補清單** → [主 README §6](../README.md)
- **規格灰色地帶（待組員討論）** → [主 README §7](../README.md)
- **本機完整啟動（含後端 + DB）** → [主 README §9](../README.md)
