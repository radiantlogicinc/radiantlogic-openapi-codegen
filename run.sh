#!/usr/bin/env bash

set -euo pipefail

mvn clean compile \
  exec:exec@generate \
  -DprogramArgs="-p=$(pwd)/radiantone-openapi-8.1.4-beta.2-SNAPSHOT.yaml"
