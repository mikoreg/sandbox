# Orbit Gate Wear — proof of concept

Jednomodułowy projekt Android Studio dla zegarków z Wear OS. Aplikacja jest napisana w **Javie** i rysuje grę bezpośrednio przez natywny `android.graphics.Canvas` w klasie `GameView`.

Projekt celowo nie zawiera dodatkowych ekranów, usług, sieci, reklam, analityki ani silnika gry. Jest odpowiednikiem jednoplikowego prototypu HTML.

## Funkcjonalność prototypu

- kropki startują w środku i poruszają się promieniście do obręczy;
- obręcz ma jedną szczelinę;
- przytrzymanie ekranu uruchamia obrót;
- puszczenie ekranu zatrzymuje obrót;
- każde kolejne przytrzymanie uruchamia obrót w przeciwnym kierunku;
- poprawne przepuszczenie kropki zwiększa wynik;
- kolizja z obręczą zabiera jedno z trzech żyć;
- po utracie wszystkich żyć dotknięcie rozpoczyna grę od nowa;
- poziom rośnie co 8 punktów i modyfikuje prędkość, częstotliwość emisji oraz szerokość szczeliny.

## Wymagania

- Android Studio z obsługą Wear OS;
- JDK 17 lub nowszy obsługiwany przez używaną wersję Android Studio;
- Android SDK Platform 35;
- Android SDK Build-Tools;
- Android SDK Platform-Tools;
- do emulatora: obraz systemu Wear OS;
- do fizycznego zegarka: Wear OS API 28 lub nowszy.

Konfiguracja projektu:

| Element | Wersja |
|---|---:|
| Android Gradle Plugin | 8.13.2 |
| Gradle Wrapper | 8.14.5 |
| Java source/target | 17 |
| `compileSdk` | 35 |
| `targetSdk` | 35 |
| `minSdk` | 28 |

## Otwarcie i zbudowanie w Android Studio

1. Rozpakuj archiwum ZIP.
2. Uruchom Android Studio.
3. Wybierz **Open** i wskaż katalog `OrbitGateWear` — ten, w którym znajdują się `settings.gradle` i `gradlew`.
4. Poczekaj na zakończenie **Gradle Sync**.
5. Gdy IDE poprosi o brakujący Android SDK 35 albo Build-Tools, wybierz instalację wymaganych składników.
6. Wybierz **Build > Make Project**.
7. Uruchom testy: kliknij prawym przyciskiem katalog `app/src/test` i wybierz **Run Tests in ...**.

Nie trzeba tworzyć `local.properties` ręcznie. Android Studio zapisze w nim lokalną ścieżkę do Android SDK. Plik jest celowo wykluczony z repozytorium i archiwum.

## Budowanie z terminala

Linux/macOS:

```bash
./gradlew clean test assembleDebug
```

Windows:

```powershell
.\gradlew.bat clean test assembleDebug
```

Debug APK powstanie w:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Projekt zawiera Gradle Wrapper, dlatego nie wymaga osobnej instalacji Gradle.

## Uruchomienie na emulatorze Wear OS

1. Otwórz **Tools > Device Manager**.
2. Wybierz **Create Device**.
3. W kategorii **Wear OS** wybierz np. profil **Wear OS Small Round**.
4. Pobierz stabilny obraz systemu Wear OS i zakończ kreator.
5. Uruchom emulator.
6. W górnym pasku Android Studio wybierz emulator jako urządzenie docelowe.
7. W konfiguracji uruchomieniowej wybierz moduł `app` i kliknij **Run**.

Android Studio zbuduje debug APK, zainstaluje go i uruchomi aktywność `MainActivity`.

## Wdrożenie na fizyczny zegarek przez Wi-Fi

### 1. Włączenie opcji programistycznych

Nazwy pozycji mogą się nieznacznie różnić zależnie od producenta.

1. Na zegarku otwórz **Ustawienia > System > Informacje**.
2. Kilkukrotnie dotknij numeru kompilacji, aż pojawi się informacja o włączeniu opcji programistycznych.
3. Wróć do ustawień i otwórz **Opcje programistyczne**.
4. Włącz **Debugowanie ADB**.
5. Włącz **Debugowanie bezprzewodowe**.
6. Komputer i zegarek muszą znajdować się w tej samej sieci Wi-Fi, która zezwala urządzeniom na wzajemną komunikację.

### 2. Parowanie ADB

Na zegarku wybierz **Debugowanie bezprzewodowe > Sparuj nowe urządzenie**. Zegarek pokaże adres IP, port parowania oraz kod.

W terminalu uruchom:

```bash
adb pair ADRES_IP:PORT_PAROWANIA
```

Po wyświetleniu pytania wpisz kod z zegarka.

### 3. Połączenie z zegarkiem

W głównym ekranie debugowania bezprzewodowego odczytaj port połączenia. Zwykle jest inny niż port parowania.

```bash
adb connect ADRES_IP:PORT_POLACZENIA
adb devices
```

Na liście powinien pojawić się zegarek ze statusem `device`.

Po połączeniu zegarek powinien również pojawić się na liście urządzeń w Android Studio. Wybierz go i kliknij **Run**.

### 4. Ręczna instalacja APK

Po zbudowaniu debug APK:

```bash
adb -s ADRES_IP:PORT_POLACZENIA install -r app/build/outputs/apk/debug/app-debug.apk
```

Uruchomienie aplikacji z terminala:

```bash
adb -s ADRES_IP:PORT_POLACZENIA shell am start \
  -n com.m1k0.orbitgate/.MainActivity
```

Odinstalowanie:

```bash
adb -s ADRES_IP:PORT_POLACZENIA uninstall com.m1k0.orbitgate
```

## Wdrożenie przez USB

Niektóre zegarki albo stacje dokujące obsługują transmisję danych przez USB.

1. Włącz debugowanie ADB na zegarku.
2. Podłącz zegarek lub stację dokującą do komputera przewodem obsługującym dane.
3. Zaakceptuj komunikat o autoryzacji debugowania na zegarku.
4. Sprawdź połączenie:

```bash
adb devices
```

5. Wybierz zegarek w Android Studio i kliknij **Run** albo użyj `adb install -r`.

## Struktura projektu

```text
OrbitGateWear/
├── app/
│   ├── build.gradle
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/m1k0/orbitgate/
│       │   │   ├── MainActivity.java
│       │   │   ├── GameView.java
│       │   │   └── GameEngine.java
│       │   └── res/
│       └── test/java/com/m1k0/orbitgate/GameEngineTest.java
├── gradle/wrapper/
├── BUILD_AND_DEPLOY.md
├── README.md
├── build.gradle
├── settings.gradle
├── gradle.properties
├── gradlew
└── gradlew.bat
```

### Najważniejsze klasy

- `MainActivity` — uruchamia pełnoekranowy `GameView` i steruje jego cyklem życia.
- `GameView` — pętla animacji, Canvas, dotyk, efekty cząsteczkowe i wibracje.
- `GameEngine` — logika niezależna od Androida, możliwa do testowania zwykłymi testami JUnit.
- `GameEngineTest` — testuje zmianę kierunku, zatrzymywanie, trafienia, błędy, koniec gry, poziomy i obliczenia kątów.

## Sterowanie

1. Naciśnij ekran i przytrzymaj — obręcz zacznie się obracać.
2. Puść ekran — obręcz zatrzyma się.
3. Naciśnij ponownie — obręcz zacznie obracać się w przeciwną stronę.
4. Po końcu gry dotknij ekran — stan gry zostanie wyzerowany.

## Diagnostyka

### Gradle Sync nie znajduje Android SDK

Otwórz **Tools > SDK Manager** i doinstaluj:

- Android SDK Platform 35;
- Android SDK Build-Tools;
- Android SDK Platform-Tools.

Następnie wybierz **File > Sync Project with Gradle Files**.

### Android Studio używa niepoprawnego JDK

Otwórz:

```text
File > Settings > Build, Execution, Deployment > Build Tools > Gradle
```

W polu **Gradle JDK** wybierz JDK dostarczony z Android Studio albo JDK 17.

### Zegarek nie jest widoczny w `adb devices`

```bash
adb kill-server
adb start-server
adb connect ADRES_IP:PORT_POLACZENIA
adb devices
```

Sprawdź również, czy:

- komputer i zegarek są w tej samej sieci;
- używasz portu połączenia, a nie portu parowania;
- debugowanie bezprzewodowe pozostaje włączone;
- sieć nie izoluje klientów Wi-Fi.

### Instalacja kończy się błędem niezgodności podpisu

Usuń poprzednią wersję aplikacji, jeżeli została zainstalowana z innym kluczem:

```bash
adb uninstall com.m1k0.orbitgate
```

Następnie uruchom instalację ponownie. Odinstalowanie usuwa lokalne dane tej aplikacji.

## Oficjalna dokumentacja

- Tworzenie i uruchamianie aplikacji Wear OS: <https://developer.android.com/training/wearables/get-started/creating>
- Debugowanie Wear OS: <https://developer.android.com/training/wearables/get-started/debugging>
- Debugowanie przez Wi-Fi: <https://developer.android.com/training/wearables/get-started/debug-wifi>
- Testy w Android Studio: <https://developer.android.com/studio/test/test-in-android-studio>
- Android Gradle Plugin: <https://developer.android.com/build/releases/gradle-plugin>
