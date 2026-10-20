# PowerNap v0.4.0-multiengine

## Why this version exists

The 2026-09-25 quiet-awake session (`20260925-131932-000-false-positive.csv`) reproduced the core limitation of the Legacy HR+ACC classifier: a relaxed, motionless awake user can still be confirmed as asleep. The old classifier is therefore retained as an experimental/reference detector, not treated as ground truth.

## New detection architecture

Sensor acquisition and sleep detection are now separate choices.

### Detection engines

- **AUTO** — uses Wear OS `USER_ACTIVITY_ASLEEP` when the device reports that capability; otherwise falls back to Legacy HR+ACC.
- **Wear OS sleep state** — only the Health Services activity-state detector may trigger the +10 minute sleep alarm. If unsupported or registration fails, the 30-minute hard deadline remains the only automatic wake-up.
- **Legacy HR + ACC** — the existing in-house classifier, retained for comparison and fallback testing.

### Shadow mode

`Run other detectors in shadow` keeps non-authoritative detectors running and logging their decisions without allowing them to schedule the nap alarm. This is enabled by default.

## Hard deadline

At every START the app immediately schedules an exact safety alarm for:

`START + 30 minutes`

When the authoritative detector later confirms sleep, the final alarm becomes:

`min(detected sleep onset + 10 minutes, START + 30 minutes)`

If a sleep event is delivered retrospectively and its +10 minute target has already passed, the app wakes as soon as possible without extending beyond a still-future hard deadline.

## Wear OS Health Services

The project now depends on:

`androidx.health:health-services-client:1.1.0`

The app checks `PassiveMonitoringCapabilities.getSupportedUserActivityStates()` for `USER_ACTIVITY_ASLEEP`, then requests `UserActivityInfo` through `PassiveMonitoringClient`.

The detector logs the Health Services `stateChangeTime` separately from receive time because the state change may be reported retrospectively.

## UI

New/changed controls:

- Detection engine
- Sensor source
- Run other detectors in shadow
- Record RAW sensor data
- Keep RAW until alarm
- CHECK DETECTORS
- visible Wear OS sleep-state capability
- visible active/selected detector
- visible hard deadline / alarm reason

## Diagnostics

New event types include:

- `DETECTION_CONFIG`
- `HARD_DEADLINE_SET`
- `WEAR_SLEEP_CAPABILITY`
- `WEAR_SLEEP_LISTENER_REGISTERED`
- `WEAR_ACTIVITY_STATE`
- `WEAR_ASLEEP_IGNORED`
- `DETECTOR_DECISION`
- `SLEEP_DECISION`
- `NAP_ALARM_SET`

The known 2026-09-25 Legacy false-positive recording is included in `powernap-sessions/` for future detector work.

## Tests

- Existing core detector smoke tests remain available.
- Previous quiet-awake regression recordings still pass the v0.3 Legacy test.
- `scripts/test-alarm-policy.sh` verifies normal +10 min scheduling, hard-deadline precedence, retrospective detection, and deadline capping.
- The 2026-09-25 recording intentionally still reproduces a Legacy false positive. v0.4 addresses it architecturally by allowing Wear OS to be authoritative while Legacy runs in shadow.
