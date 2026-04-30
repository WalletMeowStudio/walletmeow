/* =========================================================
   WalletPet — API 模組骨架
   對齊 0425WalletPet 規格資料夾 4.5 章節
   每個函式的 path 對應 Spring Boot Controller
   版本: v1.1  |  日期: 2026-04-26
   ========================================================= */
(function(){
  const api = window.WalletPet && WalletPet.api;
  if (!api) { console.error('[wp-api] shared.js must load first'); return; }

  /* ---------- Auth (第 2 組 彥祥) ---------- */
  WalletPet.authApi = {
    login    : (body) => api.post('/api/auth/login', body),
    register : (body) => api.post('/api/users/register', body),
    me       : ()     => api.get('/api/users/me'),
  };

  /* ---------- Account (第 3 組 珮倫) ---------- */
  WalletPet.accountApi = {
    list   : ()       => api.get('/api/accounts'),
    get    : (id)     => api.get(`/api/accounts/${id}`),
    create : (body)   => api.post('/api/accounts', body),
    update : (id, b)  => api.put(`/api/accounts/${id}`, b),
    remove : (id)     => api.del(`/api/accounts/${id}`),
  };

  /* ---------- Transfer / 轉帳 (第 3 組) — 規格 4.5 Transfer 模組 ----------
     ADR 0001 (Option B): 規格 4.5 原本只定義 POST /api/transfers (無 list),
       為支援 transfers.html 歷史列表 UI,提案補一隻 GET /api/transfers
     - POST /api/transfers
         body: { fromAccountId, toAccountId, transactionAmount, transactionDate, note }
         -> TransferResponse
     - GET /api/transfers (新增,後端尚未實作)
         query (全選用): ?startDate=yyyy-MM-dd&endDate=yyyy-MM-dd&accountId=int
         -> [TransferResponse]
       TransferResponse: { accountTransId, userId, fromAccountId, toAccountId,
                           transactionAmount, transactionDate, note, createdAt } */
  WalletPet.transferApi = {
    list   : (params = {}) => api.get('/api/transfers' + api.qs(params)),
    create : (body)       => api.post('/api/transfers', body),
  };

  /* ---------- Category (第 4 組 姿穎) ---------- */
  WalletPet.categoryApi = {
    list: (params = {}) =>api.get('/api/categories' + api.qs(params)),
    available: (type = '') =>api.get('/api/categories/available' + api.qs({ type })),
    get: (id) =>api.get(`/api/categories/${id}`),
    create: (body) =>api.postForm('/api/categories', {
        categoryName: body.categoryName,
        categoryType: body.categoryType,
        icon: body.icon || '',
        color: body.color || '',
      }),
    update: (id, body) =>api.putForm(`/api/categories/${id}`, {
        categoryName: body.categoryName || '',
        icon: body.icon || '',
        color: body.color || '',
        isDisable: body.isDisable,
      }),
    disable: (id) =>api.putForm(`/api/categories/${id}`, {isDisable: true,}),
  };

  /* ---------- Transaction (第 4 組) ---------- */
  WalletPet.transactionApi = {
  formMeta: (transactionType = '') =>
    api.get('/api/transactions/form-meta' + api.qs({ transactionType })),

  list: (params = {}) =>
    api.get('/api/transactions' + api.qs({
      startDate: params.startDate,
      endDate: params.endDate,
      accountId: params.accountId,
      categoryId: params.categoryId,
      type: params.type,
      page: params.page,
      size: params.size,
    })),
  summary: (params = {}) => api.get('/api/transactions/summary' + api.qs(params)),
  get: (id) => api.get(`/api/transactions/${id}`),
  create: (body) => api.post('/api/transactions', body),
  update: (id, body) => api.put(`/api/transactions/${id}`, body),
  remove: (id) => api.del(`/api/transactions/${id}`),
};

  /* ---------- Budget (第 5 組 俊廷) ---------- */
  WalletPet.budgetApi = {
    list   : ()      => api.get('/api/budgets'),
    create : (body)  => api.post('/api/budgets', body),
    update : (id, b) => api.put(`/api/budgets/${id}`, b),
    remove : (id)    => api.del(`/api/budgets/${id}`),
  };

  /* ---------- Saving Goal (第 5 組) ---------- */
  WalletPet.savingGoalApi = {
    list   : ()      => api.get('/api/saving-goals'),
    create : (body)  => api.post('/api/saving-goals', body),
    update : (id, b) => api.put(`/api/saving-goals/${id}`, b),
    remove : (id)    => api.del(`/api/saving-goals/${id}`),
    deposit: (id, amount) => api.post(`/api/saving-goals/${id}/deposit`, { amount }),
  };

  /* ---------- Pet (第 1 組 禹孜) — 規格 4.5 Pet 模組 + cancan 系統 ----------
     - GET    /api/pets/me                                        -> PetResponse (含 mood / cancan)
     - POST   /api/pets/interact          body: { interactionType }  -> PetResponse  (legacy)
     - POST   /api/pets/reward-by-bookkeeping body: { transactionId, eventType }  (legacy)
     cancan 系統新端點:
     - POST   /api/pets/feed              body: { userId, petId, feedType: CAN/FISH/SNACK/FEAST }
              -> PetResponse  (套用 -1/-10 cancan、+1/+15 mood,含每日 +3 mood 上限)
     - POST   /api/pets/claim-bookkeeping body: { userId, petId, transactionId? }
              -> PetResponse  (cancan +1,同一天最多 +5)
     - POST   /api/pets/login-tick?userId=&petId=
              -> { pet, streakDays, missedDays, moodDelta, appliedRule, firstLoginToday }
              (登入後呼叫一次,套用連續登入 +5/+10 / 缺席 -10/-20 / mood<60 連 7 拉回 60) */
  WalletPet.petApi = {
    me: () =>api.get('/api/pets/me'),
    feed: (foodType) =>api.post('/api/pets/feed' + api.qs({ foodType })),
    loginTick: (loginDate = '') =>api.post('/api/pets/login-tick' + api.qs({ loginDate })),
  };
  WalletPet.dailyRewardApi = {
    today: (date = '') =>api.get('/api/rewards/daily/today' + api.qs({ date })),
    calculate: (date = '') =>api.post('/api/rewards/daily/calculate' + api.qs({ date })),
    history: () =>api.get('/api/rewards/daily/history'),
  };
  WalletPet.petEventApi = {
    list: (params = {}) =>api.get('/api/pets/events' + api.qs(params)),
  };

  /* ---------- Chart / 圖表 (第 2, 6 組) — 規格 4.5 Chart 模組 ----------
     規格定義 7 隻 GET 端點,所有參數透過 URLSearchParams 傳遞
     - expense-pie            ?startDate&endDate[&accountId]
     - cashflow-line          ?startDate&endDate&groupBy=DAY|MONTH
     - monthly-cashflow-line  ?year
     - daily-cashflow-line    ?year&month
     - summary                ?startDate&endDate[&accountId]
     - monthly-summary        ?year&month
     - daily-summary          ?date */
  function qs(params) {
    if (!params) return '';
    const u = new URLSearchParams();
    Object.entries(params).forEach(([k, v]) => {
      if (v !== undefined && v !== null && v !== '') u.append(k, v);
    });
    const s = u.toString();
    return s ? '?' + s : '';
  }

  WalletPet.chartApi = {
    expensePie         : (params) => api.get('/api/charts/expense-pie' + qs(params)),
    cashflowLine      : (params) => api.get('/api/charts/cashflow-line' + qs(params)),
    monthlyCashflowLine: (params) => api.get('/api/charts/monthly-cashflow-line' + qs(params)),
    dailyCashflowLine  : (params) => api.get('/api/charts/daily-cashflow-line' + qs(params)),
    summary            : (params) => api.get('/api/charts/summary' + qs(params)),
    monthlySummary     : (params) => api.get('/api/charts/monthly-summary' + qs(params)),
    dailySummary       : (params) => api.get('/api/charts/daily-summary' + qs(params)),
  };
})();
