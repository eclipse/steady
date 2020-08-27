/**
 * This file is part of Eclipse Steady.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 */
package org.eclipse.steady.java.test;

import java.util.Comparator;

/**
 * Real-world example (modified).
 */
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
