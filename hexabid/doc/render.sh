#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

render_html() {
  local doc_file="$1"
  asciidoctor \
    -r asciidoctor-diagram \
    -r asciidoctor-diagram/plantuml/classpath \
    "$doc_file"
}

render_pdf() {
  local doc_file="$1"
  local pdf_file="$2"
  asciidoctor-pdf \
    -r asciidoctor-diagram \
    -r asciidoctor-diagram/plantuml/classpath \
    -a pdf-theme="$ROOT_DIR/doc/pdf-theme.yml" \
    -o "$pdf_file" \
    "$doc_file"
}

render_html "$ROOT_DIR/doc/architecture-c4.adoc"
render_pdf "$ROOT_DIR/doc/architecture-c4.adoc" "$ROOT_DIR/doc/architecture-c4.pdf"

render_html "$ROOT_DIR/doc/user-guide.adoc"
render_pdf "$ROOT_DIR/doc/user-guide.adoc" "$ROOT_DIR/doc/user-guide.pdf"
