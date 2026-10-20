#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT="$ROOT/build/core-smoke-alarm"
rm -rf "$OUT"
mkdir -p "$OUT"
javac -d "$OUT" \
  "$ROOT/app/src/main/java/com/example/powernapwatch/core/AlarmDecisionPolicy.java" \
  "$ROOT/core-smoke/AlarmDecisionPolicyTest.java"
java -cp "$OUT" AlarmDecisionPolicyTest
