#!/usr/bin/env bash

set -euo pipefail

mvn clean compile \
  exec:exec@generate \
  -DprogramArgs="-p=$(pwd)/end-to-end-tests/iddm-api-test/radiantone-openapi-8.1.4-beta.2-SNAPSHOT.yaml"

(
  cd ./rest-api-java-client-builder/output/RadiantOne-V8-API/8.1.4-beta.2-SNAPSHOT
  mvn clean install -DskipTests
)
