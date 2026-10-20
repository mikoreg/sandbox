# PowerNapWatch v0.2.2 diagnostic changes

Changes driven by the real Samsung session `20260924-201341.csv`:

- Samsung continuous trackers now request `flush()` every 5 s instead of 10 s.
- Each `flush()` request, boolean result, completion and completion latency is written as `DIAG`.
- HR and ACC callback batch sizes/timestamp ranges are written as `DIAG`.
- `onBody=true` is emitted only when wrist state changes, rather than once for every HR sample.
- IBI values in a Samsung batch are assigned approximate individual beat timestamps by walking the IBI list backwards from its DataPoint timestamp.
- Implausibly short IBI values (<400 ms) are excluded from the detector.
- RMSSD is still calculated and logged, but no longer contributes to sleep confidence until Samsung batch timing is validated with more sessions.
- HR baseline is fixed to the first 45 seconds instead of continuing to drift over the first 90 seconds.
- Estimated sleep onset can no longer be backdated before the baseline period ends.
- CSV analyzer understands `DIAG`, reports p95 delivery delay and flush diagnostics.

The detector remains experimental and is a wake/sleep heuristic, not a medical sleep-stage classifier.
