# Hexabid SPA

Angular SPA dla projektu `Hexabid`.

## Założenie kontraktowe

Frontend nie utrzymuje własnych ręcznych modeli HTTP dla API aukcyjnego.
Źródłem prawdy pozostaje OpenAPI w module backendowym `auctions-api-contract`.

Wygenerowany klient TypeScript trafia do:

`src/app/data-access/generated/auction-contract`

## Synchronizacja kontraktu

Z katalogu `hexabid-spa`:

```bash
npm run contract:sync
```

Lub z katalogu głównego repo:

```bash
mvn -pl auctions-api-contract generate-sources
```

## Uruchomienie

1. Uruchom backend na `http://localhost:8080`.
2. Uruchom frontend:

```bash
npm install
npm start
```

Dev server Angular używa proxy do backendu dla:

- `/api`
- `/oauth2`
- `/login`
- `/logout`
- `/ws-auctions`

## Build

```bash
npm run build
```
