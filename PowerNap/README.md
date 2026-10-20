# PowerNapWatch v0.2.4

## v0.2.4 — START/STOP and sleep feedback (session ground truth)

This release makes **two user-facing changes** for Wear OS / Galaxy Watch:

1. The existing main button displays `START` when idle and changes to `STOP` during `BASELINE`, `WAITING_FOR_SLEEP`, `SLEEP_CANDIDATE`, `NAPPING`, and `ALARMING`. There is no second STOP button. During an alarm, it stops vibration; while monitoring or waiting for the nap alarm, it cancels the session.
2. When the session ends (manual STOP, after an alarm is stopped, or sensor error), the watch asks **"Czy udało Ci się zasnąć?"** with **TAK / NIE** buttons. The question persists when the app is closed and reopened. Answering is required before starting the next session, so a training label is not silently discarded.

### CSV feedback

`FEEDBACK` is appended to the **same** session CSV as all previous events/sensor data. The CSV schema is unchanged (16 columns), for example:

```csv
seq,type,sensor_timestamp_ms,received_timestamp_ms,delivery_delay_ms,x,y,z,hr,ibi,status,state,confidence,motion_rms,rmssd,reason
123,FEEDBACK,1790000000000,1790000000000,0,,,,,,,CANCELLED,,,,"user_reported_sleep=NO"
```

- `user_reported_sleep=YES`: user reports they slept during the session.
- `user_reported_sleep=NO`: user reports they did not sleep (valuable false-positive example when detector predicted sleep).
- `state`: last state when the session ended, e.g. `CANCELLED`, `FINISHED`, or `ERROR`.
- The label indicates **whether sleep occurred during the session**, not the exact sleep onset timestamp or clinically verified sleep.

The **Record sensor data** switch controls detailed ACC/HR/IBI/snapshot logging only. A small **activity/event CSV is always created** so the user feedback is saved even when raw recording is OFF. No sensor samples are recorded in that mode. The analyzer now prints any `FEEDBACK` rows as well.

Use the same download command for both log types:

```bash
# Select watch if more than one ADB entry is connected
export ANDROID_SERIAL='YOUR_WATCH_SERIAL'
./scripts/pull-sessions.sh com.example.powernapwatch --latest powernap-sessions
python3 scripts/analyze-session.py powernap-sessions/YOUR_SESSION.csv
```

For the `standardDebug` APK use package name `com.example.powernapwatch.standard`. **Feedback requires a new session recorded with v0.2.4; old CSV files are not retrospectively labeled.**

### Build / install

```bash
./scripts/test-core.sh
./scripts/test-feedback.sh
./gradlew :app:assembleStandardDebug
adb install -r app/build/outputs/apk/standard/debug/app-standard-debug.apk
```

For `samsungDebug`, first place the **official** Samsung Health Sensor SDK AAR at `app/libs/samsung-health-sensor-api.aar`, then:

```bash
./scripts/check-samsung-sdk.sh
./gradlew :app:assembleSamsungDebug
adb install -r app/build/outputs/apk/samsung/debug/app-samsung-debug.apk
```

The Samsung SDK AAR is proprietary and is not included. This source archive was smoke-tested for the standalone Java detector and feedback CSV row and XML validity; Android APK compilation requires a local Android SDK (and Samsung AAR for the Samsung flavor).

---

# PowerNapWatch4 v0.2 diagnostics

This revision adds **runtime sensor-engine selection** and an explicit **Record sensor data** switch.

**Important:** the AAR that was present in the previously supplied project was a compile-only stub (its service and tracker methods were no-ops). v0.2 source archives intentionally do not ship that file. Download the official Samsung Health Sensor SDK v1.4.1 AAR and run `./scripts/check-samsung-sdk.sh` before building `samsungDebug`.

For the `samsungDebug` build the UI exposes three engines:

* `Auto (Samsung -> Wear OS fallback)` — tries Samsung Health Sensor SDK first and falls back to Android sensors.
* `Samsung Health Sensor SDK` — strict Samsung-only mode. It deliberately does **not** fall back, so Samsung connection/policy/tracker errors are visible.
* `Wear OS SensorManager` — Android `TYPE_ACCELEROMETER` + `TYPE_HEART_RATE`, useful as an A/B comparison on the same watch.

The `standardDebug` build exposes Auto and Wear OS only.

The `Record sensor data` switch is persisted. When enabled, the session CSV contains raw ACC/HR/IBI samples, detector snapshots and events. New diagnostic columns include both `sensor_timestamp_ms` and `received_timestamp_ms`; `delivery_delay_ms` makes Samsung batching, stalls and delayed callbacks directly measurable.

Pull the newest recording:

```bash
./scripts/pull-sessions.sh com.example.powernapwatch --latest
```

Pull all recordings:

```bash
./scripts/pull-sessions.sh com.example.powernapwatch --all
```

For the standard flavor use package `com.example.powernapwatch.standard`.

Analyze a recording quickly:

```bash
python3 scripts/analyze-session.py powernap-sessions/20260924-190000.csv
```

The analyzer reports missing streams, sample gaps, delivery delays, HR range, detector states and logged source errors/status events.

## Android 16 / API 36 permission fix

Because the project targets API 36, Android 16 uses `android.permission.health.READ_HEART_RATE` and `android.permission.health.READ_HEALTH_DATA_IN_BACKGROUND`. Android 15 and older continue to use `BODY_SENSORS` / `BODY_SENSORS_BACKGROUND`. v0.2 requests the permission appropriate to the watch OS version instead of always requesting the legacy permission.

---

# PowerNapWatch4

Experimental Wear OS power-nap application, designed first for **Samsung Galaxy Watch4**.

The project has two sensor flavors:

* `standardDebug` — portable Wear OS implementation using Android `SensorManager` (accelerometer + heart rate). No Samsung SDK is required.
* `samsungDebug` — Galaxy Watch4+ implementation using **Samsung Health Sensor SDK** for continuous accelerometer, HR and IBI. If the Samsung service is unavailable at runtime it falls back to standard Wear sensors.

The app does **not** claim to identify clinical sleep stages. It estimates the transition from wakefulness to sleep using wrist motion and cardiovascular signals and schedules a 10-minute alarm from the estimated onset.

## Toolchain

* Android Studio Quail / recent Android Studio
* JDK 17
* Android SDK Platform 36
* AGP 9.0.1 / Gradle 9.1-compatible environment
* A physical Galaxy Watch4 for the Samsung SDK; Samsung Health Sensor SDK does not support the emulator.

## First build: standard flavor

Open the project in Android Studio and select the `standardDebug` variant.

If you already have Gradle 9.1 installed locally, you can also run:

```bash
gradle :app:assembleStandardDebug
```

To generate a conventional Gradle wrapper once on your workstation:

```bash
gradle wrapper --gradle-version 9.1.0
./gradlew :app:assembleStandardDebug
```

APK:

```text
app/build/outputs/apk/standard/debug/app-standard-debug.apk
```

This is the easiest way to verify UI, permissions, foreground monitoring, detector state machine and alarm handling before adding Samsung's AAR.

## Samsung Health Sensor SDK build

1. Download Samsung Health Sensor SDK from Samsung Developer.
2. Copy `samsung-health-sensor-api.aar` to:

```text
app/libs/samsung-health-sensor-api.aar
```

3. Build from Android Studio (`samsungDebug`) or with local Gradle:

```bash
gradle :app:assembleSamsungDebug
```

If you generated the wrapper as shown above, use `./gradlew` instead.

APK:

```text
app/build/outputs/apk/samsung/debug/app-samsung-debug.apk
```

The Samsung flavor uses:

* `HEART_RATE_CONTINUOUS` — HR + IBI
* `ACCELEROMETER_CONTINUOUS` — 25 Hz
* an explicit `flush()` every 10 seconds while waiting for sleep, because Samsung batches continuous data when the display is off.

## Enable Samsung Health Sensor Service developer mode

On the Watch4:

1. Settings -> Apps -> Health Sensor Service (older software can show Health Platform).
2. Tap the **Health Sensor Service title area** about 10 times.
3. A Developer mode entry appears below the service version.
4. Open it, accept Samsung's partner/SDK license and enable Developer mode.

This is only for development. Distribution using Samsung Health Sensor SDK requires Samsung partner approval and registration of the final package name + signing certificate.

## Connect Galaxy Watch4 over ADB

Enable watch developer options first:

1. Settings -> About watch -> Software.
2. Tap Software version 5 times.
3. Settings -> Developer options.
4. Enable ADB debugging / wireless debugging.

Depending on the watch software version, use either Android wireless-debug pairing:

```bash
adb pair WATCH_IP:PAIR_PORT
adb connect WATCH_IP:ADB_PORT
```

or the older Samsung flow:

```bash
adb connect WATCH_IP:5555
```

Verify:

```bash
adb devices
```

Install Samsung flavor:

```bash
adb install -r app/build/outputs/apk/samsung/debug/app-samsung-debug.apk
```

## First run

The UI has an engine selector, a `Record sensor data` switch, and four actions:

* `PERMISSIONS` — grants activity, HR, notification and background health/sensor permissions. Background permission is intentionally requested as a separate step.
* `EXACT ALARM` — grants special exact-alarm access.
* `START` — starts the foreground monitoring session.
* `STOP / CANCEL` — stops monitoring or an active wake-up alarm.

The state machine is:

```text
BASELINE -> WAITING_FOR_SLEEP -> SLEEP_CANDIDATE -> SLEEP_CONFIRMED -> NAPPING -> ALARMING -> FINISHED
```

Once sleep is confirmed, sensor tracking is stopped and an exact `AlarmManager` alarm is scheduled for:

```text
estimatedSleepOnset + 10 minutes
```

The detector deliberately backdates sleep onset rather than starting the 10-minute timer at the later confirmation callback.

## Diagnostic CSV

When `Record sensor data` is enabled, the session writes raw/derived data to internal app storage:

```text
files/sessions/YYYYMMDD-HHmmss.csv
```

Pull the newest Samsung-flavor session on Linux/Fedora:

```bash
./scripts/pull-sessions.sh com.example.powernapwatch --latest
```

Pull all sessions:

```bash
./scripts/pull-sessions.sh com.example.powernapwatch --all
```

For standard flavor use package `com.example.powernapwatch.standard`. The CSV contains ACC, HR, IBI, detector snapshots, confidence, state transitions, callback receive times and delivery delays.

## Current detector heuristics

The initial model evaluates roughly every 5 seconds and uses:

* 30 s wrist-motion window,
* baseline and current median HR,
* HR trend,
* RMSSD if IBI is available,
* sustained quiet time,
* 45 s candidate confirmation with hysteresis.

These thresholds are intentionally conservative placeholders. The next engineering step is to collect several labelled sessions (`awake`, `trying to sleep`, `actual nap`) and tune them from Watch4 data rather than guessing fixed thresholds.


## Important background-sensor permission note

On Wear OS 4 through 5.1, `BODY_SENSORS_BACKGROUND` is a restricted permission. The foreground HR permission and the background permission must be granted separately. Depending on the Watch4 software build, the second step can take you to the app's sensor permission settings; choose the equivalent of **Allow all the time** for Sensors. Without this, HR access can disappear after the app UI is no longer visible.

On Wear OS 6 / API 36 the project requests `READ_HEART_RATE` and `READ_HEALTH_DATA_IN_BACKGROUND` instead.

## Gradle wrapper

The project includes the Gradle wrapper. Build with `./gradlew :app:assembleStandardDebug` or, after installing the real Samsung SDK AAR, `./gradlew :app:assembleSamsungDebug`.

## v0.2.1 diagnostic update

* `pull-sessions.sh` now uses a finite ADB timeout and explicit watch selection.
* Added `adb-watch-diagnostics.sh`.
* Samsung `SDK_POLICY_ERROR` message now includes the exact Health Sensor Service developer-mode path.
