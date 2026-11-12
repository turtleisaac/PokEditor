#!/usr/bin/env bash
# Rewrite the committed baselines. --with-mutation includes a full PIT run.
set -euo pipefail

root="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
"$root/scripts/quality/coverage.sh"
"$root/scripts/quality/check-quality.sh" --write

if [[ "${1:-}" == "--with-mutation" ]]; then
  QUALITY_MODE=full QUALITY_WRITE=1 "$root/scripts/quality/mutate.sh"
elif [[ -n "${1:-}" ]]; then
  echo "Unknown argument: $1 (expected --with-mutation)" >&2
  exit 2
fi

echo "Baselines written under .quality/. Review the diff and commit it."
