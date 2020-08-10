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
package com.sap.psr.vulas.shared.model.generic;

import static org.junit.Assert.*;

import org.junit.Test;

import com.sap.psr.vulas.shared.json.model.Version;

public class VersionTest {

    @Test
    public void test() {
        Version v0 = new Version("2.2.0");
        Version v1 = new Version("2.2.0.1");
        Version v2 = new Version("2.2.1");
        Version v3 = new Version("2.2.1.1");
        Version v4 = new Version("3.1.18");
        Version v5 = new Version("3.1.14-test-05");

        assertTrue(v0.compareTo(v1) < 0);
        assertTrue(v1.compareTo(v0) > 0);
        assertTrue(v2.compareTo(v1) > 0);
        assertTrue(v3.compareTo(v1) > 0);
        assertTrue(v4.compareTo(v5) > 0);
    }
}
