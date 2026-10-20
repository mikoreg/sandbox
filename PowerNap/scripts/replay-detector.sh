#!/usr/bin/env bash
set -euo pipefail
if [[ $# -lt 1 ]]; then
  echo "Usage: $0 SESSION.csv [SEGMENT.csv ...]" >&2
  exit 2
fi
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT="$ROOT/core-smoke/replay-out"
rm -rf "$OUT"
mkdir -p "$OUT"
javac -d "$OUT" \
  "$ROOT"/app/src/main/java/com/example/powernapwatch/core/DetectorSnapshot.java \
  "$ROOT"/app/src/main/java/com/example/powernapwatch/core/NapState.java \
  "$ROOT"/app/src/main/java/com/example/powernapwatch/core/SleepOnsetDetector.java \
  "$ROOT"/core-smoke/ReplayCsvTest.java
java -cp "$OUT" ReplayCsvTest "$@"
