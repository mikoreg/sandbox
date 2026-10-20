#!/usr/bin/env bash
set -euo pipefail
AAR="${1:-app/libs/samsung-health-sensor-api.aar}"

if [[ ! -f "$AAR" ]]; then
  echo "ERROR: Samsung SDK AAR not found: $AAR" >&2
  echo "Download the official Samsung Health Sensor SDK and copy samsung-health-sensor-api.aar into app/libs/." >&2
  exit 2
fi

TMP=$(mktemp -d)
trap 'rm -rf "$TMP"' EXIT
unzip -q "$AAR" classes.jar -d "$TMP"

OUT=$(javap -classpath "$TMP/classes.jar" -public com.samsung.android.service.health.tracking.HealthTracker 2>&1 || true)
if ! grep -q 'boolean flush();' <<<"$OUT"; then
  echo "ERROR: $AAR does not match the current Samsung Health Sensor SDK API." >&2
  echo "Expected HealthTracker.flush() to return boolean." >&2
  echo "This commonly means a compile-only stub is being used instead of the real SDK." >&2
  echo >&2
  echo "$OUT" >&2
  exit 3
fi

echo "Samsung SDK sanity check: OK"
