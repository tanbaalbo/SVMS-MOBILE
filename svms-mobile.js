/* SVMS 모바일 보기 v1.0 — made by 정산찬
 * SVMS(svms.sinokor.co.kr) 화면을 폰(사파리) 폭에 맞게 재배치한다.
 * - 화면 모양(CSS)만 바꾸고, 데이터·저장·조회 동작은 건드리지 않는다.
 * - 한 번 더 실행하거나 상단 [PC] 버튼을 누르면 원래 화면으로 돌아간다.
 */
(function () {
  if (window.__svmMobile) { window.__svmMobile.toggle(); return; }

  var VER = '1.0';
  var root = document.documentElement;
  var BP = 1100; // 이 폭(px) 이하에서만 적용

  var CSS = [
    '.svm-btn{display:none;}',
    '@media (max-width:' + BP + 'px){',
    'html.svm-on{--svm-pane-h:62vh;--svm-pane-h:62dvh;-webkit-text-size-adjust:100%;}',
    'html.svm-on,html.svm-on body{overflow-x:hidden;}',

    /* ── 왼쪽 메뉴: 평소엔 숨기고, ☰ 누르면 서랍처럼 덮어서 표시 ── */
    'html.svm-on .v-sidebar-menu.vsm_collapsed{transform:translateX(-110%);visibility:hidden;}',
    'html.svm-on .v-sidebar-menu.vsm_expanded{z-index:1001!important;width:82vw!important;max-width:300px!important;box-shadow:0 0 28px rgba(0,0,0,.35);}',
    'html.svm-on .v-sidebar-menu .vsm--link{min-height:42px;}',
    'html.svm-on .v-application--wrap>.h100p{padding-left:0!important;}',
    'html.svm-on .svm-backdrop{display:none;position:fixed;inset:0;background:rgba(0,0,0,.35);z-index:1000;}',
    'html.svm-on.svm-drawer .svm-backdrop{display:block;}',
    'html.svm-on .page-topbar .top-content{min-width:40px;min-height:36px;display:flex;align-items:center;justify-content:center;}',
    'html.svm-on .page-topbar .top-content i{font-size:26px;}',
    'html.svm-on .page-topbar .lang-icon-button{display:none;}',

    /* ── 화면 최소폭(1280px) 해제, 세로 스크롤 허용 ── */
    'html.svm-on .router-tab-page{min-width:0!important;width:100%!important;}',
    'html.svm-on .router-tab-page.u-content-layout{height:auto!important;padding-left:4px!important;padding-right:4px!important;}',
    'html.svm-on .router-tab__container{overflow-x:hidden!important;overflow-y:auto!important;-webkit-overflow-scrolling:touch;}',
    'html.svm-on .router-tab-page.u-content-layout>.d-flex.flex-column{height:auto!important;}',
    'html.svm-on .u-content-layout-content{height:calc(100vh - 120px)!important;height:calc(100dvh - 120px)!important;min-height:380px;flex:none!important;}',

    /* ── 상단 화면 제목 + 버튼줄: 줄바꿈 ── */
    'html.svm-on .u-content-layout-actions>.d-flex{flex-wrap:wrap;row-gap:4px;}',
    'html.svm-on .u-content-layout-actions .w40p,html.svm-on .u-content-layout-actions .w60p{width:auto!important;flex:1 1 auto;}',
    'html.svm-on .u-content-layout-actions .text-right{display:flex;flex-wrap:wrap;justify-content:flex-end;gap:4px;}',
    'html.svm-on .u-content-layout-actions .v-btn{min-width:0!important;margin-left:0!important;height:32px!important;padding:0 10px!important;}',
    'html.svm-on .u-card-title-wrapper{flex-wrap:wrap;row-gap:4px;}',
    'html.svm-on .u-card-title-wrapper>.ml-auto{max-width:100%;min-width:0;}',
    'html.svm-on .u-card-title-wrapper .d-flex{flex-wrap:wrap;row-gap:4px;}',
    'html.svm-on .u-card-title-wrapper .h100p.d-flex{flex-wrap:wrap;gap:4px;height:auto!important;}',

    /* ── 검색조건·입력칸 표: 항목명 위, 입력칸 아래로 쌓기 ── */
    'html.svm-on table.u-table{display:block!important;width:100%!important;max-width:100%!important;}',
    'html.svm-on table.u-table>colgroup{display:none!important;}',
    'html.svm-on table.u-table>tbody,html.svm-on table.u-table>thead{display:block!important;width:100%!important;}',
    'html.svm-on table.u-table>tbody>tr,html.svm-on table.u-table>thead>tr{display:flex!important;flex-wrap:wrap;align-items:center;gap:2px 6px;width:100%;height:auto!important;padding:1px 0;}',
    'html.svm-on table.u-table>tbody>tr>th,html.svm-on table.u-table>thead>tr>th{display:block!important;flex:0 0 100%;width:auto!important;height:auto!important;min-height:0!important;text-align:left!important;padding:6px 2px 0!important;border:0!important;background:transparent!important;}',
    'html.svm-on table.u-table>tbody>tr>th .u-label-div{white-space:normal;}',
    'html.svm-on table.u-table>tbody>tr>td,html.svm-on table.u-table>thead>tr>td{display:block!important;flex:1 1 150px;min-width:0;width:auto!important;max-width:100%;height:auto!important;}',
    'html.svm-on table.u-table td.svm-empty{display:none!important;}',
    'html.svm-on table.u-table td>.d-flex{flex-wrap:wrap;gap:4px;align-items:center;}',
    'html.svm-on table.u-table .dx-texteditor,html.svm-on table.u-table .dx-selectbox,html.svm-on table.u-table .dx-tagbox{max-width:100%;}',
    'html.svm-on table.u-table td>.d-flex:has(.dx-datebox){flex-wrap:nowrap;}',
    'html.svm-on table.u-table td>.d-flex:has(.dx-datebox)>div{flex:1 1 0;min-width:0;}',
    'html.svm-on table.u-table td>.d-flex:has(.dx-datebox)>div:not(:has(.dx-widget)){flex:0 0 auto;padding:0 2px;}',
    'html.svm-on table.u-table td>.d-flex .dx-datebox{width:100%!important;min-width:0!important;}',
    'html.svm-on table.u-table .dx-datebox input.dx-texteditor-input{min-width:0!important;width:100%;}',
    'html.svm-on table.u-table.h100p,html.svm-on table.u-table.h100p>tbody,html.svm-on table.u-table.h100p>tbody>tr{height:100%!important;}',
    'html.svm-on table.u-table.h100p>tbody>tr>td{height:100%!important;flex:1 1 100%;}',

    /* ── 좌우로 나뉜 칸(splitpanes)·나란히 놓인 칸: 위아래로 쌓기 ── */
    'html.svm-on .splitpanes{flex-direction:column!important;height:auto!important;}',
    'html.svm-on .splitpanes>.splitpanes__splitter{display:none!important;}',
    'html.svm-on .splitpanes>.splitpanes__pane{width:100%!important;height:var(--svm-pane-h)!important;flex:none!important;margin-bottom:6px;}',
    'html.svm-on .svm-stack{flex-direction:column!important;flex-wrap:nowrap!important;height:auto!important;}',
    'html.svm-on .svm-stack>*{width:100%!important;max-width:100%!important;min-width:0!important;flex:none!important;height:var(--svm-pane-h)!important;margin-bottom:6px;}',
    'html.svm-on .svm-stack>.svm-auto-h,html.svm-on .splitpanes>.splitpanes__pane.svm-auto-h{height:auto!important;}',
    /* 쌓은 칸을 품고 있는 바깥 상자들은 내용 길이만큼 늘어나게 (화면이 위아래로 스크롤됨) */
    'html.svm-on .svm-auto-chain,html.svm-on .u-content-layout-content.svm-auto-chain,html.svm-on .splitpanes>.splitpanes__pane.svm-auto-chain{height:auto!important;min-height:0!important;max-height:none!important;flex:none!important;margin-bottom:0;}',
    'html.svm-on .svm-hscroll{overflow-x:auto!important;-webkit-overflow-scrolling:touch;}',
    'html.svm-on .svm-wide-grid{min-width:var(--svm-grid-w)!important;}',

    /* ── Home: 오른쪽 달력/채팅 패널을 열면 전체 화면으로 ── */
    'html.svm-on .v-main__wrap>.w100p.h100p{grid-template-columns:100% 0px!important;}',
    'html.svm-on .v-main__wrap>.w100p.h100p:not([style*="1fr 0px"])>div:last-child{position:fixed!important;top:40px;left:0;right:0;bottom:0;z-index:900;background:#fff;overflow:auto;}',
    'html.svm-on .main-dashboard .dash-cond>.d-flex{margin-left:0!important;width:100%;}',
    'html.svm-on .dash-header,html.svm-on .dash-header-box{flex-wrap:wrap!important;gap:6px;}',
    'html.svm-on .dash-header-card{min-width:0!important;flex:1 1 40%!important;margin:0!important;}',

    /* ── 팝업: 화면 폭에 맞추기 (드롭다운·달력·툴팁 제외) ── */
    'html.svm-on .dx-popup-wrapper:not(.dx-dropdowneditor-overlay):not(.dx-datebox-wrapper):not(.dx-tooltip-wrapper):not(.dx-loadpanel-wrapper):not(.dx-toast-wrapper):not(.dx-dropdownbutton-popup-wrapper)>.dx-overlay-content{max-width:100vw!important;max-height:100vh!important;max-height:100dvh!important;}',
    'html.svm-on .svm-pop-fit>.dx-overlay-content{width:100vw!important;height:100vh!important;height:100dvh!important;transform:none!important;top:0!important;left:0!important;}',
    'html.svm-on .v-dialog:not(.v-dialog--fullscreen){margin:6px!important;max-width:calc(100vw - 12px)!important;}',
    'html.svm-on .vue-notification-group{width:auto!important;left:8px!important;right:8px!important;}',

    /* ── PC/모바일 전환 버튼 ── */
    '.svm-btn{display:inline-flex!important;align-items:center;justify-content:center;height:24px;padding:0 7px;margin-left:4px;border:1px solid #405189;border-radius:12px;color:#405189;background:#fff;font-size:11px;font-weight:600;line-height:1;cursor:pointer;-webkit-tap-highlight-color:transparent;}',
    'html:not(.svm-on) .svm-btn{position:fixed;right:8px;bottom:8px;z-index:2000;height:30px;font-size:12px;box-shadow:0 2px 8px rgba(0,0,0,.25);}',
    '}'
  ].join('\n');

  /* ── 보조 함수 ── */
  function $(sel, el) { return (el || document).querySelector(sel); }
  function $$(sel, el) { return Array.prototype.slice.call((el || document).querySelectorAll(sel)); }
  var timer = null;
  var lastPage = null;

  function addStyle() {
    var st = document.getElementById('svm-style');
    if (!st) {
      st = document.createElement('style');
      st.id = 'svm-style';
      st.textContent = CSS;
      (document.head || root).appendChild(st);
    }
  }

  var vpMeta = null, vpOrig = null;
  function setViewport(on) {
    vpMeta = vpMeta || $('meta[name=viewport]');
    if (!vpMeta) {
      vpMeta = document.createElement('meta');
      vpMeta.name = 'viewport';
      vpMeta.content = 'width=device-width, initial-scale=1';
      (document.head || root).appendChild(vpMeta);
    }
    if (vpOrig === null) vpOrig = vpMeta.content;
    // maximum-scale=1: 입력칸 누를 때 화면이 확대되는 것만 막음 (손가락 확대는 그대로 됨)
    vpMeta.content = on ? 'width=device-width, initial-scale=1, maximum-scale=1, viewport-fit=cover' : vpOrig;
  }

  function hamburger() { return $('.page-topbar .top-content i') || $('.page-topbar .top-content'); }
  function isExpanded() { var sb = $('.v-sidebar-menu'); return !!(sb && sb.classList.contains('vsm_expanded')); }
  function closeDrawer() { if (isExpanded()) { var h = hamburger(); if (h) h.click(); } }

  function ensureUi() {
    if (!$('.svm-backdrop')) {
      var bd = document.createElement('div');
      bd.className = 'svm-backdrop';
      bd.addEventListener('click', closeDrawer);
      document.body.appendChild(bd);
    }
    var btn = $('.svm-btn');
    var bar = $('.page-topbar .nav-bar');
    if (!btn) {
      btn = document.createElement('button');
      btn.type = 'button';
      btn.className = 'svm-btn';
      btn.addEventListener('click', function (e) { e.preventDefault(); e.stopPropagation(); api.toggle(); });
    }
    btn.textContent = root.classList.contains('svm-on') ? 'PC' : '모바일';
    btn.title = root.classList.contains('svm-on') ? 'PC 화면으로 보기' : '모바일 화면으로 보기';
    // 켜져 있을 땐 상단바 ☰ 옆에, 꺼져 있을 땐 오른쪽 아래에 둔다
    if (root.classList.contains('svm-on') && bar) {
      var hb = $('.top-content', bar);
      if (hb && btn.previousElementSibling !== hb) hb.insertAdjacentElement('afterend', btn);
    } else if (btn.parentNode !== document.body) {
      document.body.appendChild(btn);
    }
  }

  /* 내용이 없는 칸(빈 td)은 숨김 표시 */
  function markEmptyCells() {
    $$('table.u-table>tbody>tr>td').forEach(function (td) {
      var empty = !td.firstElementChild && !td.textContent.trim();
      if (empty !== td.classList.contains('svm-empty')) td.classList.toggle('svm-empty', empty);
    });
  }

  /* 나란히 놓인 칸 → 위아래로 쌓을 대상 표시
   *  ① w20p / w80p 처럼 폭(%)이 정해진 칸이 2개 이상
   *  ② (폰 폭에서) 달력·입력표·카드 같은 큰 덩어리가 2개 이상 나란히 있을 때 */
  var W_RE = /(^|\s)w([1-9]\d?)p(\s|$)/;
  var BLOCK = 'table.u-table,.u-card,.dx-calendar,.ag-root-wrapper,.v-tabs,.dx-datagrid';
  function markStacks() {
    $$('.u-content-layout-content .d-flex:not(.flex-column), .main-dashboard .d-flex:not(.flex-column)').forEach(function (el) {
      if (el.classList.contains('svm-stack') || el.closest('table, .ag-root-wrapper, .dx-widget, .u-card-title-wrapper, .u-content-layout-actions')) return;
      var kids = Array.prototype.filter.call(el.children, function (c) { return c.nodeType === 1; });
      if (kids.length < 2) return;
      var pct = kids.filter(function (c) { return W_RE.test(c.className || ''); });
      var blocks = kids.filter(function (c) { return c.matches(BLOCK) || c.querySelector(BLOCK); });
      if (pct.length >= 2 || (blocks.length >= 2 && el.clientWidth > 0 && el.clientWidth < 600)) el.classList.add('svm-stack');
    });
  }

  /* 쌓은 칸 중 지금 보이는 표(그리드)·편집기가 없는 칸(입력 양식·달력 등)은 내용 길이만큼만 높이를 줌 */
  var HEAVY = '.ag-root-wrapper,.dx-datagrid,.dx-treelist,.dx-treeview,.dx-scheduler,.dx-pivotgrid,.ql-container,.tox,canvas,iframe';
  var PX_H = /(^|;)\s*height:\s*\d+(\.\d+)?px/;
  // 칸 높이를 꽉 채우는(높이가 % 로 정해진) 그리드가 보이면 true
  // — 중간에 높이가 px로 고정된 상자 안에 들어 있는 그리드는 칸 높이와 상관없으므로 제외
  function hasFillHeavy(c) {
    var hs = c.querySelectorAll(HEAVY);
    for (var i = 0; i < hs.length; i++) {
      if (hs[i].offsetParent === null) continue;
      var e = hs[i], fixed = false;
      while (e && e !== c) {
        if (PX_H.test(e.getAttribute('style') || '')) { fixed = true; break; }
        e = e.parentElement;
      }
      if (!fixed) return true;
    }
    return false;
  }
  function markAutoHeights() {
    $$('.svm-stack>*, .splitpanes>.splitpanes__pane').forEach(function (c) {
      var auto = !hasFillHeavy(c);
      if (auto !== c.classList.contains('svm-auto-h')) c.classList.toggle('svm-auto-h', auto);
    });
  }

  /* 지금 보이는 쌓은 칸(splitpanes·svm-stack)을 품은 바깥 상자들 → 높이 자동
   * (탭을 바꾸면 다시 계산: 안 보이는 탭 때문에 다른 탭의 표가 납작해지지 않게) */
  function markChains() {
    var want = [];
    $$('.u-content-layout-content .splitpanes, .u-content-layout-content .svm-stack').forEach(function (t) {
      if (t.offsetParent === null) return;
      var e = t.parentElement;
      while (e) {
        if (e.classList.contains('router-tab-page')) break;
        if (!PX_H.test(e.getAttribute('style') || '') && want.indexOf(e) < 0) want.push(e);
        if (e.classList.contains('u-content-layout-content')) break;
        e = e.parentElement;
      }
    });
    $$('.svm-auto-chain').forEach(function (e) { if (want.indexOf(e) < 0) e.classList.remove('svm-auto-chain'); });
    want.forEach(function (e) { if (!e.classList.contains('svm-auto-chain')) e.classList.add('svm-auto-chain'); });
  }

  /* 왼쪽 고정 열이 화면보다 넓은 표 → 표를 넓게 펴고 좌우로 밀어서 보게 함
   * (표의 열 설정은 건드리지 않고, 보이는 폭만 넓힘) */
  function widenPinnedGrids() {
    $$('.ag-root-wrapper').forEach(function (g) {
      if (g.offsetParent === null) return;
      var theme = g.parentElement;
      if (!theme || theme.classList.contains('svm-wide-grid')) return;
      var pl = $('.ag-pinned-left-header', g), pr = $('.ag-pinned-right-header', g);
      var pin = (pl ? pl.getBoundingClientRect().width : 0) + (pr ? pr.getBoundingClientRect().width : 0);
      var w = g.getBoundingClientRect().width;
      if (w > 0 && pin > w * 0.6) {
        theme.style.setProperty('--svm-grid-w', Math.round(pin + 240) + 'px');
        theme.classList.add('svm-wide-grid');
        if (theme.parentElement) theme.parentElement.classList.add('svm-hscroll');
      }
    });
  }

  /* 큰 팝업은 화면 전체로 */
  function fitPopups() {
    $$('.dx-popup-wrapper').forEach(function (w) {
      if (/dx-dropdowneditor-overlay|dx-datebox-wrapper|dx-tooltip-wrapper|dx-loadpanel-wrapper|dx-toast-wrapper|dx-dropdownbutton-popup-wrapper|dx-context-menu|dx-dropdownlist-popup-wrapper/.test(w.className)) return;
      var c = $('.dx-overlay-content', w);
      if (!c) return;
      var wd = parseFloat(c.style.width) || c.getBoundingClientRect().width;
      var big = wd > window.innerWidth - 20;
      if (big !== w.classList.contains('svm-pop-fit')) w.classList.toggle('svm-pop-fit', big);
    });
  }

  function activePage() {
    var pages = $$('.router-tab-page');
    for (var i = 0; i < pages.length; i++) if (pages[i].offsetParent !== null) return pages[i];
    return null;
  }

  function nudge() { try { window.dispatchEvent(new Event('resize')); } catch (e) {} }

  function scan() {
    timer = null;
    if (!root.classList.contains('svm-on')) { ensureUi(); return; }
    ensureUi();
    markEmptyCells();
    markStacks();
    markAutoHeights();
    markChains();
    widenPinnedGrids();
    fitPopups();
    root.classList.toggle('svm-drawer', isExpanded());
    var p = activePage();
    if (p && p !== lastPage) { lastPage = p; setTimeout(nudge, 50); }
  }
  function schedule() { if (!timer) timer = setTimeout(scan, 250); }

  /* 메뉴에서 화면을 고르면 서랍 메뉴 자동으로 닫기 */
  document.addEventListener('click', function (e) {
    if (!root.classList.contains('svm-on')) return;
    var link = e.target.closest && e.target.closest('.v-sidebar-menu .vsm--link');
    if (link && !$('.vsm--arrow', link) && isExpanded() && window.innerWidth <= BP) setTimeout(closeDrawer, 120);
  }, true);

  // 표(그리드) 안에서 스크롤할 때마다 생기는 변화는 무시 → 폰에서 버벅이지 않게
  var NOISY = '.ag-root-wrapper,.dx-scrollable-container,.ql-editor';
  var mo = new MutationObserver(function (recs) {
    if (timer) return;
    for (var i = 0; i < recs.length; i++) {
      var t = recs[i].target;
      if (t.nodeType !== 1) t = t.parentElement;
      if (!t || !t.closest(NOISY)) { schedule(); return; }
    }
  });

  var api = {
    version: VER,
    on: function () {
      addStyle();
      root.classList.add('svm-on');
      setViewport(true);
      mo.observe(document.body, { childList: true, subtree: true, attributes: true, attributeFilter: ['class'] });
      lastPage = null;
      scan();
      setTimeout(nudge, 80);
      try { localStorage.setItem('svm-mobile', 'on'); } catch (e) {}
    },
    off: function () {
      root.classList.remove('svm-on', 'svm-drawer');
      setViewport(false);
      ensureUi();
      setTimeout(nudge, 80);
      try { localStorage.setItem('svm-mobile', 'off'); } catch (e) {}
    },
    toggle: function () { root.classList.contains('svm-on') ? api.off() : api.on(); }
  };
  window.__svmMobile = api;

  // 자동 적용(Userscripts 앱)으로 실행됐을 때 지난번에 PC로 꺼 뒀으면 꺼진 채로 시작
  var saved = null;
  try { saved = localStorage.getItem('svm-mobile'); } catch (e) {}
  if (window.__svmAuto && saved === 'off') { addStyle(); mo.observe(document.body, { childList: true, subtree: true }); ensureUi(); }
  else api.on();
})();
