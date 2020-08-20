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
package com.sap.psr.vulas.shared.enums;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

public class ScopeTest {

  @Test
  public void testFromStringArray() {
    final String[] scope_strings = new String[] {"teST", "compiLE"};
    final Set<Scope> scopes = Scope.fromStringArray(scope_strings);
    assertTrue(scopes.contains(Scope.TEST));
    assertTrue(scopes.contains(Scope.COMPILE));
    assertEquals(2, scopes.size());
  }

  @Test
  public void testFromString() {
    assertEquals(Scope.TEST, Scope.fromString("teST", null));
    assertEquals(Scope.TEST, Scope.fromString("123", Scope.TEST));
    assertEquals(null, Scope.fromString("123", null));
  }
}
