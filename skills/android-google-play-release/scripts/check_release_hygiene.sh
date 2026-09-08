#!/usr/bin/env bash
set -euo pipefail

ROOT="${1:-.}"
cd "$ROOT"

fail=0
while IFS= read -r -d '' file; do
  case "$file" in
    ./.git/*|./.gradle/*|./build/*|*/build/*) continue ;;
  esac
  echo "Suspicious release/private file: $file" >&2
  fail=1
done < <(find . -type f \( -name '*.jks' -o -name '*.keystore' -o -name '*.p12' -o -name '*.aab' -o -name '*.apk' -o -name 'gradle.properties' -o -name '*service-account*.json' \) -print0)

if git ls-files | grep -E '(^|/)([^/]+\.(jks|keystore|p12|aab|apk)|gradle\.properties|.*service-account.*\.json)$' >/dev/null 2>&1; then
  echo 'A sensitive or build artifact is tracked by Git.' >&2
  fail=1
fi

git diff --check

if [ "$fail" -ne 0 ]; then
  exit 1
fi

echo 'Release hygiene check passed.'
