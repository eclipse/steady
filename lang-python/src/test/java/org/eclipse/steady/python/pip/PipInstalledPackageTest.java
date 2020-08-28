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
package org.eclipse.steady.python.pip;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PipInstalledPackageTest {

  @Test
  public void testEquals() {
    PipInstalledPackage p1 = new PipInstalledPackage("abc-XYZ#", "1.0");
    PipInstalledPackage p2 = new PipInstalledPackage("ABC_xyz$", "2.0");

    assertFalse(p1.equals(p2));
    assertTrue(p1.equalsStandardDistributionName(p2));
  }
}
