#!/usr/bin/env bash

set -euo pipefail

major_version="$1"
minor_version="$2"
patch_version="$3"

if [ -z "$major_version" ] || [ -z "$minor_version" ] || [ -z "$patch_version" ]; then
  echo "Missing required input values" >&2
  echo "major_version=$major_version" >&2
  echo "minor_version=$minor_version" >&2
  echo "patch_version=$patch_version" >&2
  exit 1
fi

echo "Incrementing to new snapshot version"
next_snapshot_version="${major_version}.${minor_version}.$(( "$patch_version" + 1 ))-SNAPSHOT"
echo "next_snapshot_version=$next_snapshot_version" | tee -a $GITHUB_OUTPUT