package com.radiantlogic.openapi.codegen.javaclient.exceptions;

public class ModelNotFoundException extends JavaClientBuilderException {
  public ModelNotFoundException() {}

  public ModelNotFoundException(final String message) {
    super(message);
  }

  public ModelNotFoundException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public ModelNotFoundException(final Throwable cause) {
    super(cause);
  }
}
