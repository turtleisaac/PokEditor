#!/usr/bin/env bash
# Point this clone at the committed hooks in .githooks/.
set -euo pipefail

root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
git -C "$root" config core.hooksPath .githooks
echo "Git hooks path set to .githooks"
