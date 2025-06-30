package com.radiantlogic.openapi.codegen.javaclient.generate.models;

/** Defines the type of union, if any, that the model represents. */
public enum UnionType {
  /** The type is not a union type. This is valid. */
  NO_UNION,

  /**
   * The type is a union type, but has no discriminator. This means a oneOf mapping and nothing
   * else. This is invalid.
   */
  UNION_NO_DISCRIMINATOR,

  /**
   * The type is a discriminated union type but has no mapping. This means a oneOf mapping, a
   * discriminator, but no discriminator mapping. This is invalid.
   */
  DISCRIMINATED_UNION_NO_MAPPING,

  /**
   * The type is a discriminated union with its mapping. This means a oneOf mapping, a
   * discriminator, and a discriminator mapping. This is valid.
   */
  DISCRIMINATED_UNION_WITH_MAPPING
}
