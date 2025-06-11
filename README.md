# rest_api_java_client_builder

This project provides a tool that generates a Java CLI client from an OpenAPI spec.

## Running Locally

### IntelliJ

It is critical that IntelliJ is configured to use Maven, not its own internal tooling, to do all build steps. This is because of certain maven plugins that are used.

To do this, go to Settings -> Build, Execution, Deployment -> Build Tools -> Maven -> Runner, and make sure that the "Delegate IDE build/run actions to maven" checkbox is selected.
