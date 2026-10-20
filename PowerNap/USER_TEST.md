# PowerNap user-test checklist

Recommended settings on Galaxy Watch during validation:

- Read engine: `Samsung Health Sensor SDK`
- Record RAW sensor data: `ON`
- Keep RAW until alarm: `ON`
- Exact alarm permission: granted

Expected flow:

1. Press START just before trying to nap.
2. The detector collects baseline and waits for a stable sleep candidate.
3. A candidate must remain plausible for 120 seconds.
4. When sleep is confirmed, the alarm is scheduled for estimated onset + 10 minutes.
5. With `Keep RAW until alarm` enabled, sensor logging continues during the nap.
6. At the alarm boundary monitoring is stopped and the wake-up vibration starts.
7. Press STOP on the alarm.

For field reports, preserve the CSV session and note whether the alarm felt early, correct, or late.
