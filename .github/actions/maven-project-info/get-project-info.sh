#!/usr/bin/env bash

set -euo pipefail

version=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
echo "version=$version" | tee -a $GITHUB_OUTPUT