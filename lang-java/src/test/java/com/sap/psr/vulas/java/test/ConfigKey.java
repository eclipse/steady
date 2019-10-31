package com.sap.psr.vulas.java.test;

import java.io.Serializable;
import java.util.NoSuchElementException;

/** Real-world example (modified). */
@SuppressWarnings("nls")
public enum ConfigKey implements ConfigurationKey {
  MEMBER_MANAGEMENT(Boolean.class, "featureMemberManagementEnabled", "true"),
  MEMBER_METADATA_UPDATE(Boolean.class, "featureMemberMetadata", "false");

  private final Class type;

  private final String key;
  private final String defaultValue;

  ConfigKey(final Class type, final String literal, final String defaultValue) {
    this.type = type;
    this.key = literal;
    this.defaultValue = defaultValue;
  }

  @Override
  public final Class getType() {
    return type;
  }

  @Override
  public final String getKey() {

    class InnerClass1 {
      InnerClass1() {}

      void foo() {}
    };

    class InnerClass2 {
      void foo() {}
    };

    return key;
  }

  @Override
  public final String getDefaultValue() {

    class InnerClass1 {
      InnerClass1() {}

      void foo() {}
    };

    Serializable s =
        new Serializable() {
          void foo() {}
        };

    return defaultValue;
  }

  public static ConfigurationKey fromKey(final String key) {
    for (final ConfigurationKey configKey : ConfigKey.values()) {
      if (configKey.getKey().equals(key)) {
        return configKey;
      }
    }

    throw new NoSuchElementException(
        String.format("No API configuration key found with key '%s'", key));
  }
}
