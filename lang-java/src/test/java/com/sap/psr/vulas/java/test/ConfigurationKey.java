package com.sap.psr.vulas.java.test;

import java.util.Comparator;

/** Real-world example (modified). */
public interface ConfigurationKey {

  Comparator<ConfigurationKey> CONFIG_KEY_BY_LITERAL_COMPARATOR =
      new Comparator<ConfigurationKey>() {
        @Override
        public int compare(final ConfigurationKey key1, final ConfigurationKey key2) {
          return key1.getKey().compareTo(key2.getKey());
        }
      };

  Comparator<ConfigurationKey> CONFIG_KEY_BY_LITERAL_COMPARATOR_2 =
      new Comparator<ConfigurationKey>() {
        @Override
        public int compare(final ConfigurationKey key1, final ConfigurationKey key2) {
          return key1.getKey().compareTo(key2.getKey());
        }
      };

  Class getType();

  String getKey();

  String getDefaultValue();
}
