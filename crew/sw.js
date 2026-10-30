/* 승하선 순번 보기 — 오프라인용 (만든 판: d0778ebf58) */
const C="crew-rot-d0778ebf58", FILES=["./","./index.html","./manifest.webmanifest","./icon-192.png","./icon-512.png","./apple-touch-icon.png"];
self.addEventListener("install",e=>{ e.waitUntil(caches.open(C).then(c=>c.addAll(FILES)).then(()=>self.skipWaiting())); });
self.addEventListener("activate",e=>{ e.waitUntil(caches.keys().then(ks=>Promise.all(ks.filter(k=>k!==C).map(k=>caches.delete(k)))).then(()=>self.clients.claim())); });
self.addEventListener("fetch",e=>{ const u=new URL(e.request.url); if(u.origin!==location.origin) return;
  if(e.request.mode==="navigate"){ e.respondWith(fetch(e.request).catch(()=>caches.match("./index.html"))); return; }
  e.respondWith(caches.match(e.request).then(r=>r||fetch(e.request))); });
