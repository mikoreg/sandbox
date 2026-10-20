# PowerNap v0.3.0-user-test

This build switches back from overnight capture to real PowerNap behavior while retaining diagnostic controls.

## Behavior

- Alarm is always enabled and scheduled for `estimated sleep onset + 10 minutes`.
- Exact-alarm permission is required for every normal session.
- Sleep candidate confirmation was increased from 45 s to 120 s after two recorded quiet-awake false positives.
- `Keep RAW until alarm` no longer disables the alarm. It keeps ACC/HR/IBI logging alive after sleep detection and closes the monitoring service when the alarm fires.
- `Record RAW sensor data`, sensor-engine selection, permissions, and exact-alarm controls remain visible during field validation.
- Application/UI label is now `Power Nap` rather than `Power Nap Lab`.

## Replay evidence used for this build

- Both previously recorded quiet-awake sessions no longer reach `SLEEP_CONFIRMED` with the 120 s confirmation window.
- Replaying the 24/25 Sep real-sleep session with the new default gives approximately:
  - estimated onset: 23:15:24
  - confirmation: 23:17:56
  - resulting alarm target: 23:25:24
- Samsung Health placed sleep onset around 23:14 (screen-derived reference), so this deliberately conservative detector is late rather than early.

This is still a user-test build, not a medical sleep classifier.
