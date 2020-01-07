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
import com.sap.psr.vulas.shared.json.model.Exemption;
import com.sap.psr.vulas.shared.json.model.IExemption;
import com.sap.psr.vulas.shared.json.model.VulnerableDependency;
import com.sap.psr.vulas.shared.util.FileUtil;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

public class ExemptionTest {

	@Test
	public void testReadFromConfiguration() {
		final VulasConfiguration c1 = new VulasConfiguration();

		// Old formats
		c1.setProperty(Exemption.CFG_PREFIX_EXEMPTED_BUGS, "CVE-2014-0050, CVE-2014-0051"); // Will result in 2 exemptions
		c1.setProperty(Exemption.CFG_PREFIX_EXEMPTED_SCOPES, "provided, test"); // Will result in 2 exemptions

		// New format
		c1.setProperty("vulas.report.exempt.CVE-2014-0050.*.*", "Lorem ipsum");
		c1.setProperty("vulas.report.exempt.CVE-2014-0050.ABCDEFGHIJKLMNOPQRSTUVWXYZ.*", "Lorem ipsum");
		c1.setProperty("vulas.report.exempt.CVE-2014-0050.*.teST", "Lorem ipsum");
		c1.setProperty("vulas.report.exempt.*.*.proviDED", "Lorem ipsum");

		final Set<IExemption> e = Exemption.readFromConfiguration(c1.getConfiguration());
		assertEquals(8, e.size());
	}

	@Test
	public void testIsExempted() {
		try {
			// digest = 6F1EBC6CE20AD8B3D4825CEB2E625E5C432A0E10, bugId = CVE-2014-0050, scope = SYSTEM, cvssScore = 7.5, wellknownDigest = false
			final VulnerableDependency vd = (VulnerableDependency)JacksonUtil.asObject(FileUtil.readFile("./src/test/resources/vulndep.json"), VulnerableDependency.class);

			// Exempted (new format)
			assertTrue(this.getExemption("vulas.report.exempt.CVE-2014-0050.*.SYSTem", "Lorem ipsum").isExempted(vd));
			assertFalse(this.getExemption("vulas.report.exempt.CVE-2014-0051.*.TEST", "Lorem ipsum").isExempted(vd));
			assertTrue(this.getExemption("vulas.report.exempt.CVE-2014-0050.6F1EBC6CE20AD8B3D4825CEB2E625E5C432A0E10.*", "Lorem ipsum").isExempted(vd));
			assertFalse(this.getExemption("vulas.report.exempt.CVE-2014-0051.ABCDEF.*", "Lorem ipsum").isExempted(vd));

			// Exempted (old format for scopes)
			assertTrue(this.getExemption(Exemption.CFG_PREFIX_EXEMPTED_SCOPES, "sysTEM").isExempted(vd));
			assertFalse(this.getExemption(Exemption.CFG_PREFIX_EXEMPTED_SCOPES, "test").isExempted(vd));

			// Exempted (old format for bugs)
			assertTrue(this.getExemption(Exemption.CFG_PREFIX_EXEMPTED_BUGS, "CVE-2014-0050").isExempted(vd));
			assertFalse(this.getExemption(Exemption.CFG_PREFIX_EXEMPTED_BUGS, "CVE-2014-0051").isExempted(vd));
			
		} catch (IOException e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	private IExemption getExemption(String _key, String _value) {
		final VulasConfiguration cfg = new VulasConfiguration();
		cfg.setProperty(_key, _value);
		final Set<IExemption> s = Exemption.readFromConfiguration(cfg.getConfiguration());
		assertEquals(1, s.size());
		return s.iterator().next();
	}
}
