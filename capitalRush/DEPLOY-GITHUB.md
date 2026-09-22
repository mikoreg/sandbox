# Publikacja na GitHub Pages

Zakładana lokalizacja repozytorium:

```text
/work/projects/github.com/mikoreg/sandbox
```

Aplikacja znajduje się w:

```text
/work/projects/github.com/mikoreg/sandbox/capitalRush
```

## 1. Wgraj pliki

Rozpakuj zawartość paczki bezpośrednio do katalogu `capitalRush`, zastępując dotychczasowy `capital-rush.html` nową wersją.

Przykład:

```bash
cd /work/projects/github.com/mikoreg/sandbox
cp capitalRush/capital-rush.html capitalRush/capital-rush.html.bak
unzip -o ~/Downloads/capital-rush-pwa.zip -d capitalRush
```

Jeżeli ZIP pobrał się do innego katalogu, zmień ścieżkę do pliku.

## 2. Przetestuj lokalnie przez HTTP

PWA i service worker nie działają poprawnie z `file://`. Uruchom prosty serwer:

```bash
cd /work/projects/github.com/mikoreg/sandbox
python3 -m http.server 8080
```

Otwórz:

```text
http://localhost:8080/capitalRush/
```

Sama gra może działać również jako zwykły plik HTML, ale możliwość instalacji PWA wymaga HTTPS albo `localhost`.

## 3. Commit i push

```bash
cd /work/projects/github.com/mikoreg/sandbox

git status
git add capitalRush
git commit -m "Add Capital Rush PWA with local Hall of Fame"
git push
```

Jeśli pracujesz na innej gałęzi niż domyślna, wypchnij właściwą gałąź.

## 4. Włącz GitHub Pages

W repozytorium `mikoreg/sandbox` na GitHub:

1. `Settings`
2. `Pages`
3. w `Build and deployment` wybierz `Deploy from a branch`
4. wybierz gałąź, na której są pliki (zwykle `main`)
5. wybierz folder `/(root)`
6. `Save`

GitHub Pages publikuje tylko katalog główny gałęzi albo `/docs`, dlatego przy obecnej strukturze publikujemy root repozytorium, a gra pozostaje w podkatalogu `capitalRush`.

Przy standardowym adresie GitHub Pages aplikacja będzie dostępna pod adresem zbliżonym do:

```text
https://mikoreg.github.io/sandbox/capitalRush/
```

## 5. Instalacja PWA w Chrome

Po wejściu na opublikowaną stronę:

- Chrome może pokazać ikonę instalacji w pasku adresu,
- albo w ekranie startowym gry pojawi się `ZAINSTALUJ APLIKACJĘ`,
- ewentualnie użyj menu Chrome → `Zainstaluj Wyścig Stolic…` / `Zainstaluj aplikację`.

Po pierwszej publikacji lub zmianie service workera warto odświeżyć stronę raz jeszcze.

## Hall of Fame

Tabela wyników jest celowo lokalna dla przeglądarki (`localStorage`). Każdy gracz na swoim urządzeniu ma własne TOP 10. Rekord zawiera nick, liczbę punktów i czas. Punkty są głównym kryterium, a przy remisie lepszy jest krótszy czas.

Globalny ranking wspólny dla wszystkich użytkowników wymagałby backendu / bazy danych i nie jest częścią tej wersji.
