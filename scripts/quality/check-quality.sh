#!/usr/bin/env bash
# Run the crapper and dryer ratchet. Coverage reports must already exist.
set -euo pipefail

root="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
"$root/scripts/quality/bootstrap.sh"

export PYTHONPATH="$root/.quality/py${PYTHONPATH:+:$PYTHONPATH}"
exec python3 "$root/scripts/quality/check_quality.py" --root "$root" "$@"
