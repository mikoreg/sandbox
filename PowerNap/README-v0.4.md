# PowerNap Watch — v0.4.0 multiengine

Wear OS proof-of-concept for a power nap that starts its 10-minute sleep interval from a detected sleep onset rather than from the START button.

## Safety rule

Every session has an unconditional exact alarm at `START + 30 min`. A detector may replace it only with an earlier alarm derived from `sleep onset + 10 min`.

## Detection engines

The app intentionally keeps multiple sleep-onset detectors during validation:

- Wear OS Health Services `USER_ACTIVITY_ASLEEP`
- Legacy in-house HR + accelerometer classifier
- AUTO arbitration between them

Non-authoritative detectors can run in shadow mode so their outputs are recorded but cannot trigger the alarm.

## Sensor sources

The existing sensor-source choice remains independent of detection:

- Auto (Samsung -> Wear OS fallback)
- Samsung Health Sensor SDK
- Wear OS SensorManager

On Samsung builds, RAW ACC / HR / IBI can still be captured and rotated for offline analysis.

## Build

```bash
./gradlew :app:assembleSamsungDebug
```

Install:

```bash
adb -s "$ANDROID_SERIAL" install -r \
  app/build/outputs/apk/samsung/debug/app-samsung-debug.apk
```

The project now uses `androidx.health:health-services-client:1.1.0`, so the first build may need to download that Maven dependency.

## Quick verification

```bash
./scripts/test-core.sh
./scripts/test-alarm-policy.sh
```

Then on the watch use **CHECK DETECTORS** before the first nap test.

See `USER_TEST-v0.4.md` and `CHANGELOG-v0.4.0-multiengine.md`.
