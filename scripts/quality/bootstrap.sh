#!/usr/bin/env bash
# Install pinned crapper and dryer into .quality/py. Skip when the pin file is unchanged.
set -euo pipefail

root="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
req="$root/.quality/requirements.txt"
dest="$root/.quality/py"
stamp="$dest/.pins-stamp"

if command -v sha256sum >/dev/null 2>&1; then
  digest="$(sha256sum "$req" | awk '{print $1}')"
else
  digest="$(shasum -a 256 "$req" | awk '{print $1}')"
fi

if [[ -f "$stamp" && "$(cat "$stamp")" == "$digest" ]]; then
  exit 0
fi

mkdir -p "$dest"
python3 -m pip install --disable-pip-version-check --upgrade --target "$dest" -r "$req"
printf '%s\n' "$digest" > "$stamp"
