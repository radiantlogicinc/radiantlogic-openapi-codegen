#!/usr/bin/env bash

set -euo pipefail

generate() {
  local yaml
  yaml="$1"

  mvn clean compile \
    exec:exec@generate \
    -DprogramArgs="-p=$yaml"
}

generate "$(pwd)/examples/okta/idp-minimal.yaml"
generate "$(pwd)/examples/okta/management-minimal.yaml"
generate "$(pwd)/examples/okta/oauth-minimal.yaml"