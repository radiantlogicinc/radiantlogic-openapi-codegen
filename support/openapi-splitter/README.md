# openapi-splitter

This is a simple NodeJS script that will split an openapi specification. It targets a single tag in the spec and extracts all resources associated with that tag. This is useful for generating test data from OpenAPI specs delivered in a format too large to parse, such as the GitHub API.

## Requirements

- NodeJS 22

## How To Run

1. Install dependencies with `npm install`.
2. Run `npm run start -- {filepath} {tag}`

The extracted file will be written out in this directory with the name `{tag}.yaml`.

## Limitations

1. Only supports OpenAPI 3.0
2. Only supports YAML.