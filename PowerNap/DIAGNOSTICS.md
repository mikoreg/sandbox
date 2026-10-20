# Power Nap diagnostic workflow

## Why v0.2 has explicit engines

The old Samsung flavor silently fell back to Android sensors. That is convenient for end users but bad for debugging: a broken Samsung Health Sensor SDK path could look like a partially working app.

Use these modes separately:

1. **Wear OS SensorManager** — baseline. It must produce ACC and HR on a correctly permissioned Watch4. It does not normally provide IBI through this source, so RMSSD can stay empty.
2. **Samsung Health Sensor SDK** — strict. No fallback. Any Samsung service, SDK policy, capability or tracker failure ends the session with `ERROR` and records the reason if logging is enabled.
3. **Auto** — production-style behavior. Samsung is preferred; Wear OS is used if Samsung cannot start.

## Recording a useful failure

Enable **Record sensor data**, select one engine, press START and keep the session running long enough to reproduce the issue. For sleep-onset problems, collect at least:

- 2 minutes awake and relaxed,
- the whole period while trying to fall asleep,
- 1-2 minutes after waking if the alarm did not trigger correctly.

Do not switch engines during a running monitoring session; the controls are disabled while the detector is active.

## Pull files from the watch

Samsung build package:

```bash
./scripts/pull-sessions.sh com.example.powernapwatch --latest
./scripts/pull-sessions.sh com.example.powernapwatch --all
```

Standard build package:

```bash
./scripts/pull-sessions.sh com.example.powernapwatch.standard --latest
```

The files are copied into `./powernap-sessions/`.

## Quick analysis

```bash
python3 scripts/analyze-session.py powernap-sessions/<session>.csv
```

Important columns:

- `sensor_timestamp_ms` — time assigned to the sample by the sensor/SDK.
- `received_timestamp_ms` — time when Power Nap actually received the callback.
- `delivery_delay_ms` — callback latency. Large values expose batching or stalls.
- `type=ACC` — motion samples.
- `type=HR` — heart-rate samples.
- `type=IBI` — inter-beat intervals from Samsung Health Sensor SDK.
- `type=SNAP` — detector decision state and derived features.
- `type=EVENT` — engine startup/fallback/errors and session events.

## Suggested A/B test on Galaxy Watch4

Run two similarly timed sessions while seated/lying in the same position:

### A. Wear OS SensorManager

Expected:

- ACC stream present continuously,
- HR stream present after permission is granted,
- IBI usually absent,
- `active: Wear OS SensorManager` in UI.

### B. Samsung Health Sensor SDK (strict)

Expected:

- `active: Samsung Health Sensor SDK`,
- ACC,
- HR,
- IBI when valid beats are available,
- no fallback message.

If B immediately reports timeout/policy/capability errors but A works, the detector is not the first problem to solve; the Samsung SDK/service setup is.

## Samsung SDK sanity check

Before a Samsung build:

```bash
./scripts/check-samsung-sdk.sh
```

The old project contained a compile-only stub AAR. Its `connectService()` and tracker methods were no-ops, so it could compile while never connecting to Health Sensor Service. Replace it with the official SDK AAR downloaded from Samsung Developer.

## SDK_POLICY_ERROR on Galaxy Watch4

If the watch shows `Samsung SDK policy denied`, the Samsung engine did connect to Health Sensor Service, but Health Sensor Service rejected the app policy. During development this normally means **Health Sensor Service Developer mode is OFF**. This is separate from the normal Wear OS/ADB Developer options.

On the watch:

1. `Settings -> Apps -> Health Sensor Service` (some software versions may still call it `Health Platform`).
2. Tap the **Health Sensor Service title area** about 10 times.
3. Open the newly visible **Developer mode** entry.
4. Accept the Samsung Health Partner Service & SDK license.
5. Turn **Developer mode ON**.
6. Stop and restart Power Nap. If necessary, force-stop/relaunch the app or reboot the watch once.

For a publicly distributed app, Samsung requires partner approval and registration of the app package name + signing certificate SHA-256. Developer mode bypasses that policy check for local development/testing.

## pull-sessions.sh appears to hang

v0.2.1 adds an explicit ADB device selection and a default 15-second timeout. A common cause is that both a phone and watch are connected, or Wireless debugging has gone stale.

Check:

```bash
adb devices -l
```

Then use the watch serial explicitly:

```bash
./scripts/pull-sessions.sh com.example.powernapwatch --latest powernap-sessions \
  --serial 192.168.x.x:PORT
```

If it still fails:

```bash
./scripts/adb-watch-diagnostics.sh 192.168.x.x:PORT
```

The script now fails with a diagnostic instead of waiting indefinitely.

## v0.2.2: Samsung batching diagnostics

New CSV `DIAG` records include:

- `FLUSH_REQUEST HR accepted=true|false`
- `FLUSH_COMPLETE HR latency_ms=...`
- `FLUSH_REQUEST ACC accepted=true|false`
- `FLUSH_COMPLETE ACC latency_ms=...`
- `FLUSH_SKIP ... pending_ms=...`
- `HR_BATCH points=...; first=...; last=...; ibi=...; maxIbiList=...`
- `ACC_BATCH points=...; first=...; last=...`

These records allow us to distinguish sensor acquisition delay from Health Sensor Service batching or a stalled `flush()` operation.
