#!/usr/bin/env bash
set -euo pipefail
PKG="${1:-com.example.powernapwatch}"
REMOTE=$(adb shell "run-as $PKG sh -c 'ls -t files/sessions/*.csv 2>/dev/null | head -1'" | tr -d '\r')
if [[ -z "$REMOTE" ]]; then
  echo "No CSV session found for package $PKG" >&2
  exit 1
fi
OUT="$(basename "$REMOTE")"
adb exec-out run-as "$PKG" cat "$REMOTE" > "$OUT"
echo "Saved $OUT"
