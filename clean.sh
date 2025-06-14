#!/usr/bin/env bash

set -euo pipefail

if [ -d output ]; then
  rm -rf output
fi

if [ -d rest-api-java-client-builder/output ]; then
  rm -rf rest-api-java-client-builder/output
fi