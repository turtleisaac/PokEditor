#!/usr/bin/env bash
# Install the sibling libraries this pom resolves. Their style checks are their own CI.
set -euo pipefail

sibling_dir() {
  local root="$1"
  local name="$2"
  local override=""
  case "$name" in
    Nds4j)
      override="${NDS4J_DIR:-}"
      ;;
    Nds4j-ToolUI)
      override="${TOOLUI_DIR:-}"
      ;;
    PokEditor-Core)
      override="${CORE_DIR:-}"
      ;;
  esac

  if [[ -n "$override" && -f "$override/pom.xml" ]]; then
    echo "$override"
    return 0
  fi

  if [[ -f "$root/../$name/pom.xml" ]]; then
    (cd "$root/../$name" && pwd)
    return 0
  fi

  local dest="$root/.upstream/$name"
  if [[ ! -f "$dest/pom.xml" ]]; then
    mkdir -p "$root/.upstream"
    git clone --quiet --depth 1 --branch main \
      "https://github.com/turtleisaac/$name.git" "$dest"
  fi

  echo "$dest"
}

prepare_siblings() {
  local root="$1"
  local name dir
  for name in Nds4j Nds4j-ToolUI PokEditor-Core; do
    dir="$(sibling_dir "$root" "$name")"
    echo "Installing $name from $dir"
    mvn -B -ntp -f "$dir/pom.xml" -DskipTests -Dspotless.check.skip=true -Dcheckstyle.skip=true install
  done
}

maven_poms() {
  local root="$1"
  find "$root" -name pom.xml \
    -not -path '*/target/*' \
    -not -path '*/.quality/*' \
    -not -path '*/.upstream/*' | sort
}
