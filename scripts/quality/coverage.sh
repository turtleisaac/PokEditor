#!/usr/bin/env bash
# Regenerate the JaCoCo XML report crapper reads.
set -euo pipefail

root="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
# shellcheck source-path=SCRIPTDIR
# shellcheck source=java-env.sh
source "$root/scripts/quality/java-env.sh"
prepare_siblings "$root"

# dead-code tests are specified to fail, so coverage runs the rest of the suite.
while IFS= read -r pom; do
  echo "JaCoCo: $pom"
  mvn -B -ntp -f "$pom" test -DexcludedGroups=dead-code
done < <(maven_poms "$root")
