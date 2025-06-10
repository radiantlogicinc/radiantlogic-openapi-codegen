#!/usr/bin/env bash

set -euo pipefail

mvn clean compile \
  exec:exec@generate \
  -DopenapiPath=https://developer.radiantlogic.com/apiSpecifications/radiantone-openapi-8.1.3.yaml
