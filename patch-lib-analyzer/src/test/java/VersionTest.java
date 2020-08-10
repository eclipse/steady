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
import static org.junit.Assert.assertTrue;



import org.junit.Test;

import com.sap.psr.vulas.patcheval.representation.ArtifactResult2;

public class VersionTest {

    @Test
    public void compareVersionTest() {
        ArtifactResult2 ar =
                new ArtifactResult2(
                        "org.apache.cxf", "cxf-rt-rs-extension-providers", "3.1.4-sap-05", false);
        ArtifactResult2 ar1 =
                new ArtifactResult2(
                        "org.apache.cxf",
                        "cxf-rt-rs-extension-providers",
                        "3.1.1",
                        true,
                        Long.valueOf("1433534079000"));
        System.out.println(ar.compareVersion(ar1));
        assertTrue(ar.compareVersion(ar1) > 0);

        ArtifactResult2 ar3 =
                new ArtifactResult2(
                        "org.apache.httpcomponents", "httpclient", "4.2.1-atlassian-5", false);
        ArtifactResult2 ar4 =
                new ArtifactResult2("org.apache.httpcomponents", "httpclient", "4.2.6", true);
        System.out.println(ar4.compareVersion(ar3));
        assertTrue(ar4.compareVersion(ar3) > 0);
    }
}
