#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"

DEFAULT_INPUT=".local/MFG-S03E04.pl.ttml.xml"
CONFIG_PATH=".local/config.properties"
JAR_PATH="target/vocalign-0.1.0-SNAPSHOT.jar"

if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
  cat <<'USAGE'
Usage:
  ./run.sh [input-file] [app-options...]

Examples:
  ./run.sh
  ./run.sh .local/MFG-S03E04.ttml.xml
  ./run.sh .local/MFG-S03E04.pl.ttml.xml --tts-provider gtts --preview-ms 120000
USAGE
  exit 0
fi

input="$DEFAULT_INPUT"
if [[ $# -gt 0 && "${1}" != --* ]]; then
  input="$1"
  shift
fi

if [[ ! -f "$input" ]]; then
  echo "Input file not found: $input" >&2
  exit 1
fi

if [[ ! -f "$CONFIG_PATH" ]]; then
  echo "Config file not found: $CONFIG_PATH" >&2
  exit 1
fi

# Keep Maven and Java aligned with JDK 25 when present.
if [[ -z "${JAVA_HOME:-}" && -d /usr/lib/jvm/java-25-openjdk ]]; then
  export JAVA_HOME=/usr/lib/jvm/java-25-openjdk
fi

mvn -q -DskipTests package

java -jar "$JAR_PATH" \
  --config "$CONFIG_PATH" \
  --input "$input" \
  "$@"
