package com.radiantlogic.openapi.builder.javaclient.exceptions;

/** A failure in parsing an OpenAPI specification. */
public class OpenapiParseException extends JavaClientBuilderException {
  public OpenapiParseException() {}

  public OpenapiParseException(final String message) {
    super(message);
  }

  public OpenapiParseException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public OpenapiParseException(final Throwable cause) {
    super(cause);
  }
}
