# v0.2.3-nightcapture

Purpose: collect a long, real sleep reference session without letting the current false-positive-prone detector terminate acquisition.

Changes:

- Added UI switch `RAW sleep capture`.
- RAW sleep capture forces sensor CSV recording ON.
- RAW sleep capture bypasses exact-alarm requirement and schedules no 10-minute alarm.
- `SLEEP_CONFIRMED` becomes a logged marker only in capture mode; sensors and CSV continue until manual STOP.
- Added 10 MiB CSV rotation with a stable session id and global sequence counter.
- Added 5-minute `CAPTURE_HEARTBEAT` events with battery %, display interactive state, and callback ages for ACC/HR/IBI.
- Monitoring service returns `START_REDELIVER_INTENT` for better recovery from process reclamation.
- Updated `pull-sessions.sh`: latest session means all rotated segments; default ADB timeout increased to 120 s; added long options and `--session`.
- Updated `analyze-session.py` to accept multiple rotated CSVs or a directory.
- Bumped app version to `0.2.3-nightcapture` / versionCode 3.

The sleep classifier itself is intentionally unchanged. The goal of this build is data collection, not detector tuning.
