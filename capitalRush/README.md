# Wyścig Stolic — PWA

Statyczna aplikacja PWA. Nie wymaga backendu. Mapa i routing wymagają połączenia z Internetem.

## Pliki

- `capital-rush.html` — właściwa gra
- `index.html` — wejście dla GitHub Pages; przekierowuje do gry
- `manifest.webmanifest` — manifest PWA
- `sw.js` — service worker / cache aplikacji
- `icon.svg`, `icons/*` — ikony PWA
- `.nojekyll` — wyłącza przetwarzanie Jekyll dla tego katalogu

## Hall of Fame

Po ukończeniu gry aplikacja prosi o nick. Wynik jest zapisywany lokalnie w `localStorage` przeglądarki. Tabela TOP 10 jest sortowana:

1. większa liczba punktów,
2. przy remisie krótszy czas.

Wyniki nie są wysyłane na serwer i nie synchronizują się pomiędzy urządzeniami ani różnymi domenami.

## PWA

Po opublikowaniu pod HTTPS (np. GitHub Pages) Chrome może zaoferować instalację aplikacji. W samej grze pojawi się przycisk `ZAINSTALUJ APLIKACJĘ`, gdy przeglądarka zgłosi możliwość instalacji.

Mapa OpenFreeMap/OpenStreetMap oraz routing OSRM pozostają zasobami sieciowymi. Service worker cache'uje aplikację i bibliotekę MapLibre po pierwszym uruchomieniu, ale nie pobiera całej mapy do pracy offline.
