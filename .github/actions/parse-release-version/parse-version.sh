#!/usr/bin/env bash

set -euo pipefail

version="$1"

pattern='^([0-9]+)\.([0-9]+)\.([0-9]+)(-(alpha|beta|rc)\.([0-9]+))?$'
echo "Comparing $version to regex $pattern"
if [[ ! "$version" =~ $pattern ]]; then
  echo "Release version '$version' does not match semver pattern, aborting" >&2
  exit 1
fi

major_version="${BASH_REMATCH[1]}"
minor_version="${BASH_REMATCH[2]}"
patch_version="${BASH_REMATCH[3]}"
qualifier_name="${BASH_REMATCH[5]-$""}"
qualifier_version="${BASH_REMATCH[6]-$""}"

echo "major_version=$major_version" | tee -a $GITHUB_OUTPUT
echo "minor_version=$minor_version" | tee -a $GITHUB_OUTPUT
echo "patch_version=$patch_version" | tee -a $GITHUB_OUTPUT
echo "qualifier_name=$qualifier_name" | tee -a $GITHUB_OUTPUT
echo "qualifier_version=$qualifier_version" | tee -a $GITHUB_OUTPUT

if [ -n "$qualifier_name" ] || [ -n "$qualifier_version" ]; then
  echo "This pipeline does not currently support releasing with semver qualifiers. Auto-versioning will not work. Please stick to major/minor/patch versions or upgrade the pipeline." >&2
  exit 1
fi