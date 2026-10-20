#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
FILE="$ROOT/powernap-sessions/20260925-131932-000-false-positive.csv"
if [[ ! -f "$FILE" ]]; then
  echo "Missing $FILE" >&2
  exit 2
fi
OUT="$($ROOT/scripts/replay-detector.sh "$FILE")"
printf '%s\n' "$OUT"
if grep -q '^onset=' <<<"$OUT"; then
  echo "KNOWN LIMITATION REPRODUCED: Legacy HR+ACC still reports sleep for this awake session."
  exit 0
fi
echo "Legacy no longer reproduces the known false positive; review detector changes."
exit 1
