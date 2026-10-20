# v0.4 user test

## First test: capability

1. Install the Samsung debug build.
2. Open Power Nap.
3. Grant `PERMISSIONS` and `EXACT ALARM` if needed.
4. Tap **CHECK DETECTORS**.
5. Note whether the screen shows:
   - `Wear OS sleep state: SUPPORTED`, or
   - `Wear OS sleep state: NOT SUPPORTED`.

This result is important: it tells us whether the Galaxy Watch can expose Health Services `USER_ACTIVITY_ASLEEP` to this app.

## Recommended settings while validating

- **Detection engine:** `Auto (Wear OS sleep -> Legacy fallback)`
- **Sensor source:** `Samsung Health Sensor SDK`
- **Run other detectors in shadow:** ON
- **Record RAW sensor data:** ON
- **Keep RAW until alarm:** ON

## Alarm semantics

The safety alarm is armed immediately at START:

`hard deadline = START + 30 min`

If the authoritative detector finds sleep:

`normal target = detected sleep onset + 10 min`

The earlier target wins.

Examples:

- START 13:00, sleep onset 13:05 -> alarm 13:15
- START 13:00, sleep onset 13:25 -> alarm 13:30 (hard deadline)
- no sleep detected -> alarm 13:30

## What to report after each nap attempt

Please note only the human ground truth that you know:

- whether you actually fell asleep,
- roughly when you think you fell asleep, if known,
- whether the alarm was too early / about right / too late,
- whether you woke naturally before it,
- any obvious interruption or movement.

Then pull the latest session with the existing `scripts/pull-sessions.sh` helper and provide all segments from that session.
