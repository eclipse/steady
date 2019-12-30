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
package com.sap.vulas.gradle;

import org.junit.Test;

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


@GradleTestProject(name = "java-lib")
public class JavaLibTest extends VulasBaseTest {

    @Test
    public void vulasAppTest() {
        executeBuild("vulasApp", "--stacktrace", "--info");
        assertEquals(SUCCESS, buildResult.task(":vulasApp").getOutcome());
        assertTrue("Dependency/construct count mismatch", buildResult.getOutput().matches("(?s).*Save app \\[com.sap.security.vulas.gradle.plugin.test:java-lib:1.0.0\\] with \\[6\\] dependencies and \\[2\\] constructs .*"));
    }
}
