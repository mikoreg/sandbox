#!/usr/bin/env bash
set -u
SERIAL="${1:-${ANDROID_SERIAL:-}}"
if [[ -z "$SERIAL" ]]; then
  echo "Usage: $0 WATCH_SERIAL" >&2
  echo "Example: $0 192.168.1.20:5555" >&2
  adb devices -l >&2 || true
  exit 2
fi
ADB=(adb -s "$SERIAL")
echo "== adb devices =="
adb devices -l || true
echo
echo "== selected watch =="
"${ADB[@]}" get-state || true
"${ADB[@]}" shell getprop ro.product.manufacturer || true
"${ADB[@]}" shell getprop ro.product.model || true
"${ADB[@]}" shell getprop ro.build.version.release || true
"${ADB[@]}" shell getprop ro.build.version.sdk || true
echo
echo "== PowerNap package =="
"${ADB[@]}" shell pm path com.example.powernapwatch || true
"${ADB[@]}" shell run-as com.example.powernapwatch sh -c 'ls -lah files/sessions 2>/dev/null || true' || true
