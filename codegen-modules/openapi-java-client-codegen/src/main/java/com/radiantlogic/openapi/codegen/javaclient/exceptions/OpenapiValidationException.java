package com.radiantlogic.openapi.codegen.javaclient.exceptions;

/** An exception caused by a failure of the validation step for an openapi specification. */
public class OpenapiValidationException extends JavaClientBuilderException {
  public OpenapiValidationException() {}

  public OpenapiValidationException(final String message) {
    super(message);
  }

  public OpenapiValidationException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public OpenapiValidationException(final Throwable cause) {
    super(cause);
  }
}
