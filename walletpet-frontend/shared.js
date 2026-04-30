/* =========================================================
   WalletPet — Shared JavaScript
   統一工具函式、tweaks 面板、mood strip、API wrapper
   版本: v1.0  |  日期: 2026-04-23
   ========================================================= */

/* =========================================================
   1. FORMATTERS — 金額 / 日期 / 百分比
   後端 DTO 對齊: BigDecimal -> Number, LocalDate -> 'yyyy-MM-dd'
   ========================================================= */
const WalletPet = window.WalletPet || {};

WalletPet.format = {
  /**
   * 格式化金額。預期輸入為 Number (BigDecimal 經 JSON 解析)
   * @param {number} amount - 金額數字
   * @param {string} currency - 幣別代碼 (預設 TWD)
   * @param {object} opts - { showSign: boolean }
   */
  money(amount, currency = 'TWD', opts = {}) {
    if (amount === null || amount === undefined || isNaN(amount)) return '—';
    const abs = Math.abs(amount);
    const formatted = abs.toLocaleString('en-US', {
      minimumFractionDigits: 0,
      maximumFractionDigits: 2,
    });
    const symbol = WalletPet.format.currencySymbol(currency);
    const sign = opts.showSign ? (amount < 0 ? '-' : '+') : (amount < 0 ? '-' : '');
    return sign + symbol + formatted;
  },

  /** 幣別符號對應表 (預留擴充) */
  currencySymbol(code = 'TWD') {
    const map = { TWD: 'NT$', USD: '$', JPY: '¥', EUR: '€', CNY: '¥', HKD: 'HK$' };
    return map[code] || (code + ' ');
  },

  /**
   * 日期: ISO 'yyyy-MM-dd' -> 顯示格式
   * @param {string} iso - 'yyyy-MM-dd' (後端 LocalDate 格式)
   * @param {string} style - 'full' | 'short' | 'md'
   *   full: 2026-04-21
   *   short: 04-21
   *   md: 4/21
   */
  date(iso, style = 'short') {
    if (!iso) return '—';
    const m = String(iso).match(/^(\d{4})-(\d{2})-(\d{2})/);
    if (!m) return iso;
    const [, y, mo, d] = m;
    if (style === 'full') return `${y}-${mo}-${d}`;
    if (style === 'md')   return `${parseInt(mo)}/${parseInt(d)}`;
    return `${mo}-${d}`;
  },

  /**
   * 時間戳 'yyyy-MM-ddTHH:mm:ss' -> 'HH:mm'
   */
  time(iso) {
    if (!iso) return '—';
    const m = String(iso).match(/T(\d{2}):(\d{2})/);
    return m ? `${m[1]}:${m[2]}` : '';
  },

  /** 今日 ISO 'yyyy-MM-dd' (預設日期欄位用) */
  today() {
    const d = new Date();
    const pad = (n) => String(n).padStart(2, '0');
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
  },

  /** 百分比顯示: 0.68 -> '68%' */
  percent(ratio, digits = 0) {
    if (ratio === null || ratio === undefined || isNaN(ratio)) return '—';
    return (ratio * 100).toFixed(digits) + '%';
  },
};


/* =========================================================
   2. CONSTANTS — 對齊 SQL / 後端 DTO 的列舉值
   後端分工文件規定: categoryType / transactionType / isLiability
   / isDisplayed 不能各組各寫各的
   ========================================================= */
WalletPet.constants = {
  TRANSACTION_TYPE: { EXPENSE: 'EXPENSE', INCOME: 'INCOME' },
  CATEGORY_TYPE:    { EXPENSE: 'EXPENSE', INCOME: 'INCOME' },
  BUDGET_SCOPE:     { MONTH: 'MONTH', WEEK: 'WEEK', CUSTOM: 'CUSTOM' },
  BUDGET_TARGET:    { CATEGORY: 'CATEGORY', TOTAL: 'TOTAL' },
  USER_ROLE:        { USER: 'USER', ADMIN: 'ADMIN' },
  CURRENCY_DEFAULT: 'TWD',
};


/* =========================================================
   3. AUTH / API WRAPPER
   - base URL: http://localhost:8080 (Spring Boot dev default)
   - 認證: JWT in localStorage
   - 回傳格式: ApiResponse<T> = { success, message, data }
   ========================================================= */
WalletPet.api = (function(){
  const BASE_URL = 'http://localhost:8080';
  const TOKEN_KEY = 'walletpet.jwt';

  function getToken() { return localStorage.getItem(TOKEN_KEY); }
  function setToken(t) { localStorage.setItem(TOKEN_KEY, t); }
  function clearToken() { localStorage.removeItem(TOKEN_KEY); }

  async function request(method, path, body) {
    const headers = { 'Content-Type': 'application/json' };
    const token = getToken();
    if (token) headers['Authorization'] = 'Bearer ' + token;

    const res = await fetch(BASE_URL + path, {
      method,
      headers,
      body: body ? JSON.stringify(body) : undefined,
    });

    // 若後端未啟動,給出明確錯誤 (而不是 fetch 的原生錯誤)
    if (!res.ok && res.status === 0) {
      throw new Error('無法連接後端 ' + BASE_URL + ' · 請確認 Spring Boot 已啟動');
    }

    const json = await res.json().catch(() => ({}));
    if (!res.ok || json.success === false) {
      const msg = json.message || res.statusText || 'Request failed';
      throw new Error(msg);
    }
    return json.data;
  }

  return {
    baseUrl: BASE_URL,
    tokenKey: TOKEN_KEY,
    getToken, setToken, clearToken,
    get:  (path)       => request('GET',    path),
    post: (path, body) => request('POST',   path, body),
    put:  (path, body) => request('PUT',    path, body),
    del:  (path)       => request('DELETE', path),
  };
})();


/* =========================================================
   4. MOOD STRIP (pets/dashboard 共用)
   ========================================================= */
WalletPet.renderMoodStrip = function(stripId, moods) {
  const strip = document.getElementById(stripId);
  if (!strip) return;
  const defaults = ['#6aa35d','#6aa35d','#e8c55a','#6aa35d','#e8643a','#6aa35d',
                    '#e8c55a','#6aa35d','#6aa35d','#e8c55a','#6aa35d','#6aa35d',
                    '#e8643a','#e8c55a','#6aa35d','#6aa35d','#e8c55a','#6aa35d',
                    '#6aa35d','#e8643a'];
  const list = moods || defaults;
  strip.innerHTML = '';
  list.forEach((c, i) => {
    const d = document.createElement('div');
    d.style.cssText = `width:28px;height:28px;border:1.5px solid var(--ink);border-radius:6px;background:${c};font-family:'Kalam';font-size:10px;display:grid;place-items:center;color:#00000090`;
    d.textContent = i + 1;
    strip.appendChild(d);
  });
};


/* =========================================================
   5. TWEAKS PANEL (開發用視覺調整,正式版可隱藏)
   ========================================================= */
WalletPet.initTweaks = function() {
  const defaults = {
    accent: '#e8643a', accentSoft: '#ffd9c8',
    sketchy: true, compact: false, labels: true,
  };
  const state = Object.assign({}, defaults);

  function apply() {
    document.documentElement.style.setProperty('--accent', state.accent);
    document.documentElement.style.setProperty('--accent-soft', state.accentSoft);
    document.body.classList.toggle('clean', !state.sketchy);
    document.body.classList.toggle('compact', state.compact);
    document.querySelectorAll('.block-label, .tag').forEach(el => {
      el.style.display = state.labels ? '' : 'none';
    });
    document.querySelectorAll('#swatches .sw').forEach(s => {
      s.classList.toggle('sel', s.dataset.c === state.accent);
    });
    const skBox = document.getElementById('sketchy');
    const cpBox = document.getElementById('compact');
    const lbBox = document.getElementById('labels');
    if (skBox) skBox.checked = state.sketchy;
    if (cpBox) cpBox.checked = state.compact;
    if (lbBox) lbBox.checked = state.labels;
  }

  const tweaksEl = document.getElementById('tweaks');
  if (!tweaksEl) return;

  apply();

  function persist(edits) {
    try { window.parent.postMessage({ type: '__edit_mode_set_keys', edits }, '*'); } catch(e){}
  }

  document.querySelectorAll('#swatches .sw').forEach(s => {
    s.addEventListener('click', () => {
      state.accent = s.dataset.c;
      state.accentSoft = s.dataset.s;
      apply();
      persist({ accent: state.accent, accentSoft: state.accentSoft });
    });
  });
  const sk = document.getElementById('sketchy');
  const cp = document.getElementById('compact');
  const lb = document.getElementById('labels');
  if (sk) sk.addEventListener('change', e => { state.sketchy = e.target.checked; apply(); persist({ sketchy: state.sketchy }); });
  if (cp) cp.addEventListener('change', e => { state.compact = e.target.checked; apply(); persist({ compact: state.compact }); });
  if (lb) lb.addEventListener('change', e => { state.labels  = e.target.checked; apply(); persist({ labels:  state.labels  }); });

  window.addEventListener('message', (e) => {
    const d = e.data || {};
    if (d.type === '__activate_edit_mode')   tweaksEl.classList.add('open');
    if (d.type === '__deactivate_edit_mode') tweaksEl.classList.remove('open');
  });
  try { window.parent.postMessage({ type: '__edit_mode_available' }, '*'); } catch(e){}
};


/* =========================================================
   5.5 PAGINATION HELPER — 共用分頁工具
   用法:
     const pg = WalletPet.createPager({ pageSize: 10 });
     pg.setItems(fullArray);         // 設定資料
     pg.goTo(1);                     // 切頁
     pg.visible();                   // 取得當前頁資料
     pg.renderInto(hostId, onChange) // 渲染 < 1 2 3 > 按鈕
   ========================================================= */
WalletPet.createPager = function(opts = {}) {
  const state = {
    items: [],
    pageSize: opts.pageSize || 10,
    current: 1,
  };

  function totalPages() {
    return Math.max(1, Math.ceil(state.items.length / state.pageSize));
  }

  function clampCurrent() {
    const max = totalPages();
    if (state.current > max) state.current = max;
    if (state.current < 1) state.current = 1;
  }

  function setItems(arr) {
    state.items = Array.isArray(arr) ? arr : [];
    clampCurrent();
  }

  function goTo(p) {
    state.current = Math.max(1, Math.min(p, totalPages()));
  }

  function visible() {
    const start = (state.current - 1) * state.pageSize;
    return state.items.slice(start, start + state.pageSize);
  }

  function renderInto(host, onChange, opts = {}) {
    const el = typeof host === 'string' ? document.getElementById(host) : host;
    if (!el) return;

    const tp = totalPages();
    // 只有 1 頁時不顯示 pager (省版面), 除非 opts.alwaysShow = true
    if (!opts.alwaysShow && tp <= 1 && state.items.length <= state.pageSize) {
      el.innerHTML = '';
      return;
    }

    // 智慧頁碼:當前頁附近 ±2,加上首尾
    const windowSize = 2;
    const pages = new Set();
    pages.add(1);
    pages.add(tp);
    for (let i = state.current - windowSize; i <= state.current + windowSize; i++) {
      if (i >= 1 && i <= tp) pages.add(i);
    }
    const sorted = Array.from(pages).sort((a, b) => a - b);

    let html = '';
    html += `<button class="pg-nav" data-nav="prev" ${state.current === 1 ? 'disabled' : ''} aria-label="Previous page">‹</button>`;

    let prev = 0;
    sorted.forEach(n => {
      if (n - prev > 1) html += `<span class="pg-gap">…</span>`;
      html += `<button class="pg-num${n === state.current ? ' active' : ''}" data-page="${n}">${n}</button>`;
      prev = n;
    });

    html += `<button class="pg-nav" data-nav="next" ${state.current === tp ? 'disabled' : ''} aria-label="Next page">›</button>`;
    html += `<span class="pg-info">${state.items.length ? ((state.current - 1) * state.pageSize + 1) : 0}-${Math.min(state.current * state.pageSize, state.items.length)} / ${state.items.length}</span>`;

    el.innerHTML = html;

    // 綁定事件 (避免重複綁定,先移除舊的)
    el.onclick = (e) => {
      const btn = e.target.closest('button');
      if (!btn || btn.disabled) return;
      if (btn.dataset.nav === 'prev') goTo(state.current - 1);
      else if (btn.dataset.nav === 'next') goTo(state.current + 1);
      else if (btn.dataset.page) goTo(Number(btn.dataset.page));
      if (typeof onChange === 'function') onChange();
    };
  }

  return {
    state,
    setItems, goTo, visible, totalPages, renderInto,
    get current() { return state.current; },
    get pageSize() { return state.pageSize; },
    set pageSize(v) { state.pageSize = v; clampCurrent(); },
  };
};



/* =========================================================
   LOGOUT — 清 JWT + user,回到 login 頁
   其他頁面無法直接呼叫,只能透過 toolbar 選單
   ========================================================= */
WalletPet.logout = function() {
  try {
    // 1. 清 token (localStorage)
    if (WalletPet.api && WalletPet.api.clearToken) {
      WalletPet.api.clearToken();
    } else {
      localStorage.removeItem('walletpet.jwt');
    }
    // 2. 清 user 資訊 + mood 快取
    localStorage.removeItem('walletpet.user');
    localStorage.removeItem('walletpet.mood');
    // 3. (可選) 通知後端 — 如果後端有 /api/auth/logout endpoint 可解除 token
    //    目前後端分工文件沒規劃,先保留空殼以利未來啟用
    //    try { await WalletPet.api.post('/api/auth/logout'); } catch(e){}
  } catch(e) {
    console.warn('[logout] 清除本地資料失敗', e);
  }
  // 4. 導回登入頁 (用 replace 不留歷史紀錄,避免按上一頁回到登入後的頁)
  location.replace('login.html');
};


WalletPet.initPageNav = function() {
  const kebabBtn = document.getElementById('kebabBtn');
  const menu = document.getElementById('kebabMenu');
  if (!kebabBtn || !menu) return;

  kebabBtn.addEventListener('click', (e) => {
    e.stopPropagation();
    menu.classList.toggle('open');
  });

  // 點菜單內項目:攔截 logout,其他照原本連結走
  menu.addEventListener('click', (e) => {
    const item = e.target.closest('.kebab-item');
    if (!item) return;
    if (item.dataset.nav === 'logout') {
      e.preventDefault();
      WalletPet.logout();
    }
  });

  // 點菜單外關閉
  document.addEventListener('click', (e) => {
    if (!menu.contains(e.target) && e.target !== kebabBtn) {
      menu.classList.remove('open');
    }
  });

  // Esc 關閉
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') menu.classList.remove('open');
  });
};


/* =========================================================
   7. PARTIALS — 浮動 toolbar 與 tweaks 面板
   每一頁只要放:
     <div data-wp-toolbar data-page="xxx"></div>
     <div data-wp-tweaks></div>
   即可自動注入
   ========================================================= */
WalletPet.PAGES = [
  { value: 'dashboard',    label: '🏠 Dashboard' },
  { value: 'transactions', label: '💸 Transactions' },
  { value: 'accounts',     label: '🏦 Accounts' },
  { value: 'transfers',    label: '↔ Transfers' },
  { value: 'categories',   label: '🏷 Categories' },
  { value: 'goals',        label: '🎯 Goals & Budgets' },
  { value: 'pets',         label: '🐾 Pets' },
  { value: 'analytics',    label: '📊 Analytics' },
  { value: 'logout',       label: '🚪 Logout', isLogout: true },
];

WalletPet.renderHeader = function(currentPage) {
  // 找錨點元素 (1) 優先用顯式指定的 [data-wp-toolbar-anchor] (login 頁用)
  //          (2) 否則自動注入到 .page-title (所有其他頁預設行為)
  //          (3) 最後 fallback 到舊的 [data-wp-toolbar] placeholder
  const explicitAnchor = document.querySelector('[data-wp-toolbar-anchor]');
  const pageTitle      = document.querySelector('main > .page-title');
  const legacyHost     = document.querySelector('[data-wp-toolbar]') || document.querySelector('[data-wp-header]');
  const anchor         = explicitAnchor || pageTitle;

  const page = currentPage ||
    (anchor   && anchor.getAttribute('data-page')) ||
    (legacyHost && legacyHost.getAttribute('data-page')) ||
    '';

  // login 頁不顯示任何 toolbar (未登入狀態不該有頁面切換入口)
  if (page === 'login') {
    if (legacyHost) legacyHost.remove();
    return;
  }

  const menuItems = WalletPet.PAGES.map(p => {
    if (p.isLogout) {
      // Logout: 不導航,由 initPageNav 攔截清 token 後才跳
      return `<a href="#" class="kebab-item logout-item" data-nav="logout">${p.label}</a>`;
    }
    return `<a href="${p.value}.html" class="kebab-item${p.value === page ? ' current' : ''}" data-nav="${p.value}">${p.label}</a>`;
  }).join('');

  // 登入頁還沒認證,不顯示 mood/cancan chip (雙重保險,雖然上面已 return)
  const isLoginPage = page === 'login';
  // mood + cancan 共用一塊 chip,連結都到 pets 頁
  // 順序: 🥫 cancan 在左、🐾 mood 在右,對齊 pets 頁 Actions 按鈕的 (-1 / +1) 寫法
  const statusChips = isLoginPage ? '' : `
    <a href="pets.html" class="chip accent" id="cancanChip" title="罐罐數">🥫 cancan —</a>
    <a href="pets.html" class="chip accent" id="moodChip" title="前往 Pets 頁">🐾 mood —</a>`;

  const toolbarHTML = `
<div class="wp-toolbar">
  ${statusChips}
  <div class="kebab-wrap">
    <button class="kebab" id="kebabBtn" aria-label="Open menu" aria-haspopup="true">
      <span class="kebab-dots"><span></span><span></span><span></span></span>
    </button>
    <nav class="kebab-menu" id="kebabMenu" role="menu">
      ${menuItems}
    </nav>
  </div>
</div>`;

  if (anchor) {
    // explicit anchor (login) 插開頭讓它在右上角; page-title 插尾部讓它在標題行最右
    const insertPosition = explicitAnchor ? 'afterbegin' : 'beforeend';
    anchor.insertAdjacentHTML(insertPosition, toolbarHTML);
    // 移除舊的占位 slot(如果有)
    if (legacyHost && legacyHost !== anchor) legacyHost.remove();
  } else if (legacyHost) {
    legacyHost.outerHTML = toolbarHTML;
  }
};

WalletPet.renderTweaksPanel = function() {
  const host = document.querySelector('[data-wp-tweaks]');
  if (!host) return;
  host.outerHTML = `
<div class="tweaks" id="tweaks">
  <h4>Tweaks</h4>
  <div class="row" style="justify-content:space-between">
    <span>Accent</span>
    <div class="swatches" id="swatches">
      <span class="sw sel" style="background:#e8643a" data-c="#e8643a" data-s="#ffd9c8"></span>
      <span class="sw" style="background:#2f6f4f" data-c="#2f6f4f" data-s="#cfe6d9"></span>
      <span class="sw" style="background:#3559a8" data-c="#3559a8" data-s="#d4dff5"></span>
      <span class="sw" style="background:#8a4fbf" data-c="#8a4fbf" data-s="#e4d3f2"></span>
    </div>
  </div>
  <div class="row"><span>Sketchy lines</span><input type="checkbox" id="sketchy" checked/></div>
  <div class="row"><span>Compact density</span><input type="checkbox" id="compact"/></div>
  <div class="row"><span>Show block labels</span><input type="checkbox" id="labels" checked/></div>
</div>`;
};


/* =========================================================
   8. CURRENT USER / PET MOOD
   mood 儲存策略:
     - 'walletpet.user' 存登入當下的使用者資訊 (姓名/role/初始 mood)
     - 'walletpet.mood' 存最新的 pet mood (每次 interact / petApi.me 後更新)
   這樣各頁 header 的 mood chip 都能顯示正確值
   ========================================================= */
WalletPet.refreshHeaderUser = function() {
  // avatar 首字
  try {
    const stored = localStorage.getItem('walletpet.user');
    if (stored) {
      const u = JSON.parse(stored);
      const av = document.getElementById('userAvatar');
      if (av && u.userName) av.textContent = u.userName[0].toUpperCase();
    }
  } catch(e) {}

  // mood chip: 優先讀 walletpet.mood,沒有再 fallback 到 user.mood
  const mc = document.getElementById('moodChip');
  if (mc) {
    let mood = null;
    const cached = localStorage.getItem('walletpet.mood');
    if (cached !== null && cached !== '') {
      const n = Number(cached);
      if (!isNaN(n)) mood = n;
    }
    if (mood === null) {
      try {
        const u = JSON.parse(localStorage.getItem('walletpet.user') || '{}');
        if (typeof u.mood === 'number') mood = u.mood;
      } catch(e) {}
    }
    mc.textContent = mood === null ? '🐾 mood —' : `🐾 mood ${mood}`;
  }

  // cancan chip: 優先讀 walletpet.cancan,沒有再 fallback 到 user.cancan
  const cc = document.getElementById('cancanChip');
  if (cc) {
    let cancan = null;
    const cached = localStorage.getItem('walletpet.cancan');
    if (cached !== null && cached !== '') {
      const n = Number(cached);
      if (!isNaN(n)) cancan = n;
    }
    if (cancan === null) {
      try {
        const u = JSON.parse(localStorage.getItem('walletpet.user') || '{}');
        if (typeof u.cancan === 'number') cancan = u.cancan;
      } catch(e) {}
    }
    cc.textContent = cancan === null ? '🥫 cancan —' : `🥫 cancan ${cancan}`;
  }
};

/** 任何頁面更新 pet 後呼叫這個,同步 header chip + localStorage */
WalletPet.updateMood = function(newMood) {
  if (typeof newMood !== 'number') return;
  localStorage.setItem('walletpet.mood', String(newMood));
  const mc = document.getElementById('moodChip');
  if (mc) mc.textContent = `🐾 mood ${newMood}`;
};

WalletPet.updateCancan = function(newCancan) {
  if (typeof newCancan !== 'number') return;
  localStorage.setItem('walletpet.cancan', String(newCancan));
  const cc = document.getElementById('cancanChip');
  if (cc) cc.textContent = `🥫 cancan ${newCancan}`;
};

/** 一次套用 PetResponse 的 mood + cancan,並寫回 localStorage / header chip
 *  另外把 petId / userId 也寫進 localStorage,讓其他頁(如 transactions)能直接拿來呼叫
 *  /api/pets/claim-bookkeeping 等需要 petId 的端點。 */
WalletPet.updatePetStatus = function(pet) {
  if (!pet || typeof pet !== 'object') return;
  if (typeof pet.mood   === 'number') WalletPet.updateMood(pet.mood);
  if (typeof pet.cancan === 'number') WalletPet.updateCancan(pet.cancan);
  if (pet.petId)  localStorage.setItem('walletpet.petId',  String(pet.petId));
  if (pet.userId) localStorage.setItem('walletpet.userId', String(pet.userId));
};


/* =========================================================
   9. AUTO-INIT on DOMContentLoaded
   ========================================================= */
document.addEventListener('DOMContentLoaded', () => {
  WalletPet.renderHeader();
  WalletPet.renderTweaksPanel();
  WalletPet.initTweaks();
  WalletPet.initPageNav();
  WalletPet.refreshHeaderUser();

  // 背景刷新:非 login 頁都嘗試拉最新 pet mood + cancan
  // - 失敗不影響頁面 (localStorage 快取值仍會顯示)
  // - pets 頁自己會 loadPet,這裡當作備援
  const mc = document.getElementById('moodChip');
  if (mc && WalletPet.petApi && typeof WalletPet.petApi.me === 'function') {
    WalletPet.petApi.me()
      .then(p => {
        WalletPet.updatePetStatus(p);
      })
      .catch(() => {
        // 後端未連通或未登入 — 若連快取都沒有,給 demo 預設值
        const cachedM = localStorage.getItem('walletpet.mood');
        if (cachedM === null || cachedM === '') WalletPet.updateMood(60);    // demo: 預設 mood 60
        const cachedC = localStorage.getItem('walletpet.cancan');
        if (cachedC === null || cachedC === '') WalletPet.updateCancan(0);   // demo: 預設 cancan 0
      });
  }
});

window.WalletPet = WalletPet;
