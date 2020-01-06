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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Set;

import org.junit.Test;

import com.sap.psr.vulas.shared.json.JacksonUtil;
import com.sap.psr.vulas.shared.json.model.BugExemption;
import com.sap.psr.vulas.shared.json.model.VulnerableDependency;
import com.sap.psr.vulas.shared.util.FileUtil;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

public class BugExemptionTest {

	@Test
	public void testReadFromConfiguration() {
		final VulasConfiguration c1 = new VulasConfiguration();
		c1.setProperty("vulas.report.exceptionExcludeBugs.CVE-2014-0050", "Foo");
		c1.setProperty("vulas.report.exceptionExcludeBugs.CVE-2014-0050.*", "Bar");
		c1.setProperty("vulas.report.exceptionExcludeBugs.CVE-2014-0050.ABCDEFGHIJKLMNOPQRSTUVWXYZ", "Lorem ipsum");
		final Set<BugExemption> e = BugExemption.readFromConfiguration(c1.getConfiguration());
		assertEquals(3, e.size());
	}

	@Test
	public void testIsExempted() {
		try {
			final VulnerableDependency vd = (VulnerableDependency)JacksonUtil.asObject(FileUtil.readFile("./src/test/resources/vulndep.json"), VulnerableDependency.class);

			// Not exempted (wrong digest)
			final VulasConfiguration c1 = new VulasConfiguration();
			c1.setProperty("vulas.report.exceptionExcludeBugs.CVE-2014-0050.ABCDEFGHIJKLMNOPQRSTUVWXYZ", "Lorem ipsum");
			Set<BugExemption> e = BugExemption.readFromConfiguration(c1.getConfiguration());
			assertEquals(1, e.size());
			assertFalse(e.iterator().next().isExempted(vd));

			// Exempted (digest *)
			final VulasConfiguration c2 = new VulasConfiguration();
			c2.setProperty("vulas.report.exceptionExcludeBugs.CVE-2014-0050.*", "Lorem ipsum");
			e = BugExemption.readFromConfiguration(c2.getConfiguration());
			assertEquals(1, e.size());
			assertTrue(e.iterator().next().isExempted(vd));

			// Exempted (correct digest)
			final VulasConfiguration c3 = new VulasConfiguration();
			c3.setProperty("vulas.report.exceptionExcludeBugs.CVE-2014-0050.6F1EBC6CE20AD8B3D4825CEB2E625E5C432A0E10", "Lorem ipsum");
			e = BugExemption.readFromConfiguration(c3.getConfiguration());
			assertEquals(1, e.size());
			assertTrue(e.iterator().next().isExempted(vd));
		} catch (IOException e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
}
