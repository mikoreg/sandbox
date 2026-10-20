#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT="$(mktemp -d)"
trap 'rm -rf "$OUT"' EXIT
javac -d "$OUT" \
  "$ROOT/app/src/main/java/com/example/powernapwatch/util/FeedbackCsvRow.java" \
  "$ROOT/core-smoke/FeedbackCsvRowTest.java"
java -cp "$OUT" FeedbackCsvRowTest
