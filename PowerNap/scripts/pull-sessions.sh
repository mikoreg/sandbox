#!/usr/bin/env bash
set -euo pipefail

PKG="com.example.powernapwatch"
MODE="--latest"
OUT_DIR="powernap-sessions"
SERIAL="${ANDROID_SERIAL:-}"
ADB_TIMEOUT="15"

usage() {
  cat <<USAGE
Usage:
  $0 [PACKAGE] [--latest|--all] [OUT_DIR] [--serial SERIAL] [--timeout SECONDS]

Examples:
  $0
  $0 com.example.powernapwatch --latest
  $0 com.example.powernapwatch --all powernap-sessions
  $0 com.example.powernapwatch --latest powernap-sessions --serial 192.168.1.20:5555

If ANDROID_SERIAL is set, it is used unless --serial is supplied.
USAGE
}

# Backwards-compatible positional arguments first.
if [[ $# -gt 0 && "$1" != --* ]]; then PKG="$1"; shift; fi
if [[ $# -gt 0 && ( "$1" == "--latest" || "$1" == "--all" ) ]]; then MODE="$1"; shift; fi
if [[ $# -gt 0 && "$1" != --* ]]; then OUT_DIR="$1"; shift; fi

while [[ $# -gt 0 ]]; do
  case "$1" in
    --serial) SERIAL="${2:?Missing value for --serial}"; shift 2 ;;
    --timeout) ADB_TIMEOUT="${2:?Missing value for --timeout}"; shift 2 ;;
    -h|--help) usage; exit 0 ;;
    *) echo "Unknown argument: $1" >&2; usage >&2; exit 2 ;;
  esac
done

mkdir -p "$OUT_DIR"

if ! command -v adb >/dev/null 2>&1; then
  echo "ERROR: adb not found in PATH" >&2
  exit 1
fi
if ! command -v timeout >/dev/null 2>&1; then
  echo "ERROR: GNU timeout not found. Install coreutils or run the adb commands manually." >&2
  exit 1
fi

# Pick a device explicitly. This avoids adb waiting/ambiguity when both phone and watch are connected.
if [[ -z "$SERIAL" ]]; then
  mapfile -t DEVICES < <(adb devices | awk 'NR>1 && $2=="device" {print $1}')
  if [[ ${#DEVICES[@]} -eq 0 ]]; then
    echo "ERROR: no online adb device found." >&2
    adb devices -l >&2 || true
    exit 1
  elif [[ ${#DEVICES[@]} -gt 1 ]]; then
    echo "ERROR: multiple adb devices are online. Select the watch with --serial SERIAL." >&2
    adb devices -l >&2 || true
    exit 1
  fi
  SERIAL="${DEVICES[0]}"
fi

ADB=(adb -s "$SERIAL")
run_adb() {
  timeout --foreground "${ADB_TIMEOUT}s" "${ADB[@]}" "$@"
}

echo "ADB device: $SERIAL"
if ! run_adb get-state >/dev/null; then
  echo "ERROR: watch is not responding to adb within ${ADB_TIMEOUT}s." >&2
  adb devices -l >&2 || true
  echo "Tip: reconnect Wireless debugging / adb connect, then retry." >&2
  exit 1
fi

# Verify run-as early so a non-debuggable/wrong package fails immediately instead of looking like a hang.
if ! run_adb shell run-as "$PKG" sh -c 'pwd >/dev/null' >/dev/null 2>&1; then
  echo "ERROR: 'run-as $PKG' failed. Is the debug build installed and is the package name correct?" >&2
  exit 1
fi

list_remote() {
  run_adb shell run-as "$PKG" sh -c 'ls -1t files/sessions/*.csv 2>/dev/null' | tr -d '\r'
}

REMOTE_LIST=""
if ! REMOTE_LIST="$(list_remote)"; then
  rc=$?
  echo "ERROR: listing session logs failed (adb timeout or connection error, rc=$rc)." >&2
  exit 1
fi

mapfile -t FILES <<< "$REMOTE_LIST"
if [[ ${#FILES[@]} -eq 0 || -z "${FILES[0]:-}" ]]; then
  echo "No CSV sessions found for package $PKG" >&2
  exit 1
fi

if [[ "$MODE" != "--all" ]]; then
  FILES=("${FILES[0]}")
fi

for remote in "${FILES[@]}"; do
  [[ -z "$remote" ]] && continue
  out="$OUT_DIR/$(basename "$remote")"
  tmp="$out.part"
  rm -f "$tmp"
  echo "Pulling $remote ..."
  if run_adb exec-out run-as "$PKG" cat "$remote" > "$tmp"; then
    mv "$tmp" "$out"
    size=$(wc -c < "$out" | tr -d ' ')
    echo "Saved $out ($size bytes)"
  else
    rc=$?
    rm -f "$tmp"
    echo "ERROR: reading $remote failed or timed out after ${ADB_TIMEOUT}s (rc=$rc)." >&2
    echo "Current adb devices:" >&2
    adb devices -l >&2 || true
    exit "$rc"
  fi
done
