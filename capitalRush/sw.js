const VERSION = 'capital-rush-v1.0.0';
const APP_CACHE = VERSION + '-app';
const RUNTIME_CACHE = VERSION + '-runtime';

const APP_SHELL = [
  './',
  './index.html',
  './capital-rush.html',
  './manifest.webmanifest',
  './icon.svg',
  './icons/icon-192.png',
  './icons/icon-512.png',
  './icons/icon-512-maskable.png'
];

self.addEventListener('install', event => {
  event.waitUntil(
    caches.open(APP_CACHE)
      .then(cache => cache.addAll(APP_SHELL))
      .then(() => self.skipWaiting())
  );
});

self.addEventListener('activate', event => {
  event.waitUntil(
    caches.keys()
      .then(keys => Promise.all(keys
        .filter(key => key.startsWith('capital-rush-') && key !== APP_CACHE && key !== RUNTIME_CACHE)
        .map(key => caches.delete(key))))
      .then(() => self.clients.claim())
  );
});

self.addEventListener('fetch', event => {
  const request = event.request;
  if (request.method !== 'GET') return;

  const url = new URL(request.url);

  // Nawigacja: sieć najpierw, a przy braku sieci ostatnia zapisana wersja aplikacji.
  if (request.mode === 'navigate') {
    event.respondWith(
      fetch(request)
        .then(response => {
          const copy = response.clone();
          caches.open(APP_CACHE).then(cache => cache.put(request, copy));
          return response;
        })
        .catch(async () => (await caches.match(request)) || caches.match('./capital-rush.html'))
    );
    return;
  }

  // Własne pliki aplikacji: cache-first z odświeżeniem w tle.
  if (url.origin === self.location.origin) {
    event.respondWith(
      caches.match(request).then(cached => {
        const network = fetch(request).then(response => {
          if (response && response.ok) {
            const copy = response.clone();
            caches.open(APP_CACHE).then(cache => cache.put(request, copy));
          }
          return response;
        }).catch(() => cached);
        return cached || network;
      })
    );
    return;
  }

  // Biblioteka MapLibre z CDN: po pierwszym pobraniu może zostać użyta z pamięci podręcznej.
  if (url.hostname === 'unpkg.com') {
    event.respondWith(
      caches.open(RUNTIME_CACHE).then(async cache => {
        const cached = await cache.match(request);
        if (cached) return cached;
        const response = await fetch(request);
        if (response) cache.put(request, response.clone());
        return response;
      })
    );
    return;
  }

  // Kafelki mapy i routing pozostają sieciowe. Nie próbujemy masowo cache'ować mapy.
  event.respondWith(fetch(request));
});
