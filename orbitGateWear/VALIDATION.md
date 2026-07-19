# Zakres weryfikacji projektu

Przed spakowaniem projektu wykonano następujące kontrole:

- kompilacja wszystkich klas Java 17 względem minimalnego zestawu sygnatur Android API używanych przez projekt;
- uruchomienie testu dymnego czystej klasy `GameEngine` obejmującego zmianę kierunku, zatrzymanie, poprawne trafienie i koniec gry po trzech błędach;
- parsowanie wszystkich plików XML;
- sprawdzenie obecności punktu wejścia `org.gradle.wrapper.GradleWrapperMain` w pliku Wrapper JAR;
- sprawdzenie składni skryptu `gradlew` przez `sh -n`;
- sprawdzenie kompletności wymaganych plików projektu.

Pełną weryfikację środowiskową należy wykonać po otwarciu projektu na komputerze z Android SDK Platform 35:

```bash
./gradlew clean test assembleDebug
```

Następnie należy uruchomić APK na emulatorze Wear OS i na docelowym zegarku zgodnie z `BUILD_AND_DEPLOY.md`.
