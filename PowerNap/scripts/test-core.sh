#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT="$ROOT/core-smoke/out"
rm -rf "$OUT"
mkdir -p "$OUT"
javac -d "$OUT" \
  "$ROOT"/app/src/main/java/com/example/powernapwatch/core/DetectorSnapshot.java \
  "$ROOT"/app/src/main/java/com/example/powernapwatch/core/NapState.java \
  "$ROOT"/app/src/main/java/com/example/powernapwatch/core/SleepOnsetDetector.java \
  "$ROOT"/core-smoke/DetectorSmokeTest.java \
  "$ROOT"/core-smoke/DetectorNoSleepSmokeTest.java
java -cp "$OUT" DetectorSmokeTest
java -cp "$OUT" DetectorNoSleepSmokeTest
