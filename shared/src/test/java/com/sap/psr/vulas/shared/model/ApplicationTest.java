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
package com.sap.psr.vulas.shared.model;

import org.junit.Test;

import com.sap.psr.vulas.shared.json.model.Application;
import com.sap.psr.vulas.shared.util.Constants;

public class ApplicationTest {

  @Test(expected = IllegalArgumentException.class)
  public void testArgumentNull() {
    new Application(null, "foo", "bar");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testArgumentTooLong() {
    final StringBuilder g = new StringBuilder();
    for (int i = 0; i <= Constants.MAX_LENGTH_GROUP + 10; i++) g.append("a");
    new Application(g.toString(), "foo", "bar");
  }
}
