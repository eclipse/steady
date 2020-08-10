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
package com.sap.psr.vulas.backend.cve;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CveTest {

    @Test
    public void testExtractCveIdentifier() {
        assertEquals("CVE-2014-0050", Cve.extractCveIdentifier("cVe-2014-0050"));
        assertEquals("CVE-2014-0050", Cve.extractCveIdentifier("cVe-2014-0050a"));
        assertEquals(null, Cve.extractCveIdentifier("cVe-2014-000a"));
        assertEquals("CVE-2014-005001010", Cve.extractCveIdentifier("cVe-2014-005001010-Foo"));
        assertEquals(null, Cve.extractCveIdentifier("Foo-cVe-2014-0050a"));
    }
}
