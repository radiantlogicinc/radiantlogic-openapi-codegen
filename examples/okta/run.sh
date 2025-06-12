#!/usr/bin/env bash

set -euo pipefail

java -jar \
  openapi-generator-7.13.0.jar \
  -i oauth-minimal.yaml \
  -l java