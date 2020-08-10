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
package com.sap.psr.vulas.cia.model.maven;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.sap.psr.vulas.shared.json.model.Artifact;
import com.sap.psr.vulas.shared.json.model.LibraryId;

public class ArtifactTest {

    @Test
    public void testVersionComparison() {

        Artifact d1 = new Artifact();
        LibraryId l = new LibraryId("test", "test", "1.1");
        d1.setTimestamp(12345L);
        d1.setLibId(l);

        Artifact d2 = new Artifact();
        LibraryId l2 = new LibraryId("test", "test", "1.1-rc1");
        d2.setTimestamp(12345L);
        d2.setLibId(l2);

        Artifact d3 = new Artifact();
        LibraryId l3 = new LibraryId("test", "test", "1.1-beta-1");
        d3.setTimestamp(12345L);
        d3.setLibId(l3);

        Artifact d4 = new Artifact();
        LibraryId l4 = new LibraryId("test", "test", "1.1-beta1");
        d4.setTimestamp(12345L);
        d4.setLibId(l4);

        Artifact d5 = new Artifact();
        LibraryId l5 = new LibraryId("test", "test", "1.10");
        d5.setTimestamp(12345L);
        d5.setLibId(l5);

        Artifact d6 = new Artifact();
        LibraryId l6 = new LibraryId("test", "test", "1.9");
        d6.setTimestamp(12345L);
        d6.setLibId(l6);

        Artifact d7 = new Artifact();
        LibraryId l7 = new LibraryId("test", "test", "1.1.1");
        d7.setTimestamp(12345L);
        d7.setLibId(l7);

        // 1.10 > 1.1
        assertTrue(d5.compareTo(d6) > 0);

        // 1.1.1 > 1.1
        assertTrue(d7.compareTo(d1) > 0);

        // 1.1 > 1.1-RC
        assertTrue(d1.compareTo(d2) > 0);
        assertTrue(d2.compareTo(d1) < 0);
        assertTrue(d2.compareTo(d2) == 0);

        // 1.1 > 1.1-beta
        assertTrue(d1.compareTo(d3) > 0);
        assertTrue(d1.compareTo(d4) > 0);
        assertTrue(d3.compareTo(d1) < 0);
        assertTrue(d3.compareTo(d4) < 0);

        // 1.1-rc > 1.1-beta-1
        assertTrue(d2.compareTo(d3) > 0);
        assertTrue(d3.compareTo(d2) < 0);

        // 1.1-rc > 1.1-beta1
        assertTrue(d2.compareTo(d4) > 0);
        assertTrue(d4.compareTo(d2) < 0);
    }
}
