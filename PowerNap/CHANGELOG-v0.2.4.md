# v0.2.4

- One primary action button: START while idle, STOP while monitoring / napping / alarming.
- On end of session, persistent Polish feedback prompt: **Czy udało Ci się zasnąć? TAK / NIE**.
- User response is appended to the original session CSV with `type=FEEDBACK`, original schema, incremented `seq` and final state.
- Activity CSV is saved even when detailed sensor recording is OFF; such sessions contain events and feedback only.
- UI hides diagnostics/permissions controls while feedback is pending, to fit a round screen.
- Added CSV feedback row smoke test and analyzer output for FEEDBACK.
- No changes to the sleep-onset classification thresholds.
- No automatic 30-minute monitoring timeout added; sessions end on user STOP, confirmed sleep + alarm, or error.
