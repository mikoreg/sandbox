# Orbit Gate Wear — proof of concept

Natywny, jednomodułowy projekt Android Studio dla zegarków z Wear OS.

- język: **Java**;
- renderowanie: własny `View` i `android.graphics.Canvas`;
- sterowanie: przytrzymanie uruchamia obrót, puszczenie zatrzymuje, kolejne przytrzymanie zmienia kierunek;
- bez Compose, WebView, silnika gry, sieci, reklam i analityki;
- zawiera testy jednostkowe czystej logiki gry.

Szczegółowa instrukcja budowania, uruchamiania na emulatorze i wdrażania na fizyczny zegarek znajduje się w pliku:

**[BUILD_AND_DEPLOY.md](BUILD_AND_DEPLOY.md)**

Podstawowa weryfikacja:

```bash
./gradlew clean test assembleDebug
```

Wynikowy APK:

```text
app/build/outputs/apk/debug/app-debug.apk
```
