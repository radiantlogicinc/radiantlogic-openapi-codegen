#!/usr/bin/env bash

set -euo pipefail

generate() {
  local yaml
  yaml="$1"

  mvn clean compile \
    exec:exec@generate \
    -DprogramArgs="-p=$yaml"
}

install() {
  local path
  path="$1"
  (
    cd "$path"
    mvn clean install -DskipTests
  )
}

generate "$(pwd)/examples/okta/idp-minimal.yaml"
#install ./rest-api-java-client-builder/output/MyAccount-Management/2025.01.1
#
generate "$(pwd)/examples/okta/management-minimal.yaml"
#install ./rest-api-java-client-builder/output/Okta-Admin-Management/2025.01.1
#
generate "$(pwd)/examples/okta/oauth-minimal.yaml"
#install ./rest-api-java-client-builder/output/Okta-OpenID-Connect--OAuth-2.0/2025.01.1

generate "$(pwd)/examples/iddm/radiantone-openapi-8.1.4-beta.2-SNAPSHOT.yaml"
#install ./rest-api-java-client-builder/output/RadiantOne-V8-API/8.1.4-beta.2-SNAPSHOT