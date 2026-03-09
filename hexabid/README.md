# Hexabid

Wzorcowa aplikacja aukcyjna z architekturą heksagonalną, podejściem contract-first i "krzyczącą" domeną.

## Moduły

- `auctions-core` - czysta domena i use case'y bez Springa/JPA, wraz ze wspólnymi archetypami biznesowymi jak `Party`, `Product` i `Lot`.
- `auctions-auth-core` - model uwierzytelnionego użytkownika i port dostępu do aktualnej tożsamości, mapowanej na domenowe `PartyId`.
- `auctions-api-contract` - kontrakt OpenAPI dla wejścia REST i generowane DTO/interfejsy.
- `auctions-adapter-in-auth-oauth` - adapter Spring Security OAuth2/OpenID Connect z dostawcami Google i GitHub.
- `auctions-adapter-in-rest` - implementacja wygenerowanego delegate REST.
- `auctions-adapter-in-ws` - inbound WebSocket dla licytacji real-time.
- `auctions-adapter-in-job` - scheduler zamykający przeterminowane aukcje.
- `auctions-adapter-out-db` - adapter JPA implementujący port repozytorium.
- `auctions-adapter-out-kafka` - publikacja zdarzeń domenowych do Kafki.
- `auctions-adapter-out-kyc` - klient KYC wygenerowany z kontraktu OpenAPI.
- `auctions-bootstrap` - composition root i uruchamialna aplikacja Spring Boot.
- `auctions-architecture-tests` - reguły ArchUnit pilnujące granic architektury.

## Uruchomienie

```bash
mvn clean verify
mvn -f auctions-bootstrap/pom.xml spring-boot:run
```

REST startuje pod `http://localhost:8080`, WebSocket STOMP pod `ws://localhost:8080/ws-auctions`.

### Profil developerski z danymi demo

Profil `dev` zasila pustą bazę H2 przykładowymi aukcjami gotowymi do przeglądania w SPA.

Uruchomienie:

```bash
mvn -f auctions-bootstrap/pom.xml spring-boot:run -Dspring-boot.run.profiles=dev
```

W tym profilu:

- seed danych jest włączony
- publiczne `GET /api/auctions` i `GET /api/auctions/{id}` są dostępne do przeglądania rynku
- dostępny jest developerski provider uwierzytelniania `dev`

Developer login:

- otwórz `http://localhost:8080/dev-auth?redirect=http://localhost:4200/`
- wybierz jednego z użytkowników demo
- po zalogowaniu możesz zmieniać użytkownika z poziomu tego samego ekranu lub z linku `Zmien usera` w SPA
- użytkownicy oznaczeni jako `KYC blocked` pozwalają testować scenariusze negatywne

## Uwierzytelnianie

Aplikacja używa OAuth2 / OpenID Connect:

- Google
- GitHub

Konfiguracja klientów jest w `auctions-bootstrap/src/main/resources/application.yaml` i korzysta ze zmiennych:

- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`
- `GITHUB_CLIENT_ID`
- `GITHUB_CLIENT_SECRET`

Tożsamość użytkownika nie jest już przekazywana w payloadach REST/WS. Adaptery wejściowe pobierają ją z kontekstu uwierzytelnienia.

## Dokumentacja

- architektura C4: `doc/architecture-c4.adoc`
- instrukcja użytkownika: `doc/user-guide.adoc`

Renderowanie:

```bash
./doc/render.sh
```
