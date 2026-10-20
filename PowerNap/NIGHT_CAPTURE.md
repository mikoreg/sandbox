# Historical note: v0.2.3 night-capture mode

The no-alarm overnight mode was used only to collect the first full-night reference recording.
It is **not** the behavior of v0.3.0-user-test.

For current use see `USER_TEST.md`. In v0.3.0 the alarm is always scheduled for
`estimated sleep onset + 10 minutes`. The `Keep RAW until alarm` switch only keeps
raw sensor recording alive through the nap so field behavior can still be validated.
