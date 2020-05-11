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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.github.packageurl.MalformedPackageURLException;
import com.sap.psr.vulas.shared.enums.Scope;
import com.sap.psr.vulas.shared.json.JacksonUtil;
import com.sap.psr.vulas.shared.json.model.ExemptionBug;
import com.sap.psr.vulas.shared.json.model.ExemptionScope;
import com.sap.psr.vulas.shared.json.model.ExemptionSet;
import com.sap.psr.vulas.shared.json.model.ExemptionUnassessed;
import com.sap.psr.vulas.shared.json.model.IExemption;
import com.sap.psr.vulas.shared.json.model.VulnerableDependency;
import com.sap.psr.vulas.shared.util.FileUtil;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

public class IExemptionTest {

	@Test
	public void testReadFromConfiguration() {
		final VulasConfiguration c1 = new VulasConfiguration();

		// Bug exemption: Old format
		c1.setProperty(ExemptionBug.DEPRECATED_CFG_PREFIX, "CVE-2014-0050, CVE-2014-0051"); // Will result in 2 exemptions

		// Bug exemption: New format
		c1.setProperty(ExemptionBug.CFG_PREFIX + ".CVE-2014-0052.reason", "Lorem ipsum");

		c1.setProperty(ExemptionBug.CFG_PREFIX + ".CVE-2014-0053.reason", "Lorem ipsum");
		c1.setProperty(ExemptionBug.CFG_PREFIX + ".CVE-2014-0053.libraries", "*, 6F1EBC6CE20AD8B3D4825CEB2E625E5C432A0E10, pkg:maven/org.apache.xmlgraphics/batik-anim@1.9.1"); // Will result in 3 exemptions

		c1.setProperty(ExemptionBug.CFG_PREFIX + ".CVE-2014-0054.reason", "Lorem ipsum");
		c1.setProperty(ExemptionBug.CFG_PREFIX + ".CVE-2014-0054.libraries", "pkg:maven/g@1.9.1, pkg:npm/g@1.9.1"); // No exemptions due to wrong PURLs
		
		// Scope exemption: Old format
		c1.setProperty(ExemptionScope.DEPRECATED_CFG, "teST, PROVided"); // Will result in 2 exemptions

		// Scope exemption: New format
		c1.setProperty(ExemptionScope.CFG, "sysTEM");

		// Unassessed exemption
		c1.setProperty(ExemptionUnassessed.CFG, "aLL");

		final ExemptionSet e = ExemptionSet.createFromConfiguration(c1.getConfiguration());
		assertEquals(10, e.size());
	}
	
	@Test
	public void testReadFromMap() {
		final Map<String, String> map = new HashMap<String, String>();
		
		// Bug exemption: Old format
		map.put(ExemptionBug.DEPRECATED_CFG_PREFIX, "CVE-2014-0050, CVE-2014-0051"); // Will result in 2 exemptions

		// Bug exemption: New format
		map.put(ExemptionBug.CFG_PREFIX + ".CVE-2014-0052.reason", "Lorem ipsum");

		map.put(ExemptionBug.CFG_PREFIX + ".CVE-2014-0053.reason", "Lorem ipsum");
		map.put(ExemptionBug.CFG_PREFIX + ".CVE-2014-0053.libraries", "*, 6F1EBC6CE20AD8B3D4825CEB2E625E5C432A0E10, pkg:maven/org.apache.xmlgraphics/batik-anim@1.9.1"); // Will result in 3 exemptions

		map.put(ExemptionBug.CFG_PREFIX + ".CVE-2014-0054.reason", "Lorem ipsum");
		map.put(ExemptionBug.CFG_PREFIX + ".CVE-2014-0054.libraries", "pkg:maven/g@1.9.1, pkg:npm/g@1.9.1"); // No exemptions due to wrong PURLs
		
		// Scope exemption: Old format
		map.put(ExemptionScope.DEPRECATED_CFG, "teST, PROVided"); // Will result in 2 exemptions

		// Scope exemption: New format
		map.put(ExemptionScope.CFG, "sysTEM");

		// Unassessed exemption
		map.put(ExemptionUnassessed.CFG, "aLL");

		final ExemptionSet e = ExemptionSet.createFromMap(map);
		assertEquals(10, e.size());
	}

	@Test
	public void testIsExempted() {
		try {
			// digest = 6F1EBC6CE20AD8B3D4825CEB2E625E5C432A0E10, bugId = CVE-2014-0050,
			// scope = SYSTEM, cvssScore = 7.5, wellknownDigest = false, affected_version_confirmed = 0
			// libid = stuff:stuff:stuff
			final VulnerableDependency vd = (VulnerableDependency)JacksonUtil.asObject(FileUtil.readFile("./src/test/resources/vulndep.json"), VulnerableDependency.class);

			// * exemptions
			assertTrue( this.getExemption(ExemptionBug.CFG_PREFIX, "CVE-2014-0050", "Lorem ipsum", "*").isExempted(vd));
			assertFalse(this.getExemption(ExemptionBug.CFG_PREFIX, "CVE-2014-0051", "Lorem ipsum", "*").isExempted(vd));

			// Digest exemptions
			assertTrue( this.getExemption(ExemptionBug.CFG_PREFIX, "CVE-2014-0050", "Lorem ipsum", "6F1EBC6CE20AD8B3D4825CEB2E625E5C432A0E10").isExempted(vd));
			assertFalse(this.getExemption(ExemptionBug.CFG_PREFIX, "CVE-2014-0050", "Lorem ipsum", "ABCDEFGHIJKLMNOPQRSTUVWXYZ").isExempted(vd));

			// Purl exemptions
			assertTrue( this.getExemption(ExemptionBug.CFG_PREFIX, "CVE-2014-0050", "Lorem ipsum", "pkg:maven/stuff/stuff@stuff").isExempted(vd));
			assertTrue( this.getExemption(ExemptionBug.CFG_PREFIX, "CVE-2014-0050", "Lorem ipsum", "pkg:maven/stuff/stuff").isExempted(vd));
			assertFalse(this.getExemption(ExemptionBug.CFG_PREFIX, "CVE-2014-0050", "Lorem ipsum", "pkg:maven/stuff/stuff@1.2.3").isExempted(vd));
			assertFalse(this.getExemption(ExemptionBug.CFG_PREFIX, "CVE-2014-0050", "Lorem ipsum", "pkg:maven/stuff/foo@stuff").isExempted(vd));

			// Old format for bugs
			assertTrue( this.getExemption(ExemptionBug.DEPRECATED_CFG_PREFIX, "CVE-2014-0050", "Lorem ipsum", null).isExempted(vd));
			assertFalse(this.getExemption(ExemptionBug.DEPRECATED_CFG_PREFIX, "CVE-2014-0051", "Lorem ipsum", null).isExempted(vd));

			// Scope
			assertTrue(new ExemptionScope(Scope.SYSTEM).isExempted(vd));
			assertFalse(new ExemptionScope(Scope.COMPILE).isExempted(vd));

			// Unassessed
			assertTrue(new ExemptionUnassessed(ExemptionUnassessed.Value.ALL).isExempted(vd));
			assertFalse(new ExemptionUnassessed(ExemptionUnassessed.Value.KNOWN).isExempted(vd));
		} catch (IOException e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	private IExemption getExemption(String _prefix, String _vuln, String _reason, String _library) {
		final VulasConfiguration cfg = new VulasConfiguration();
		if(ExemptionBug.CFG_PREFIX.equals(_prefix)) {
			cfg.setProperty(ExemptionBug.CFG_PREFIX + "." + _vuln + "." + "reason", _reason);
			cfg.setProperty(ExemptionBug.CFG_PREFIX + "." + _vuln + "." + "libraries", _library);
		} else if (ExemptionBug.DEPRECATED_CFG_PREFIX.equals(_prefix)) {
			cfg.setProperty(ExemptionBug.DEPRECATED_CFG_PREFIX, _vuln);
			cfg.setProperty(ExemptionBug.DEPRECATED_CFG_PREFIX + "." + _vuln, _reason);
		}

		final Set<IExemption> s = ExemptionBug.readFromConfiguration(cfg.getConfiguration());
		assertEquals(1, s.size());
		return s.iterator().next();
	}

	@Test
	public void testSerialization() {
		final VulasConfiguration c1 = new VulasConfiguration();
		c1.setProperty(ExemptionBug.DEPRECATED_CFG_PREFIX, "CVE-2014-0050, CVE-2014-0051"); // Will result in 2 exemptions
		final ExemptionSet e1 = ExemptionSet.createFromConfiguration(c1.getConfiguration());
		assertEquals(2, e1.size());

		final VulasConfiguration c2 = new VulasConfiguration();
		c2.setProperty(ExemptionScope.CFG, "sysTEM, provIDED"); // Will result in 2 exemptions
		final ExemptionSet e2 = ExemptionSet.createFromConfiguration(c2.getConfiguration());
		assertEquals(2, e2.size());

		final VulasConfiguration c3 = new VulasConfiguration();
		c3.setProperty(ExemptionBug.CFG_PREFIX + ".CVE-2014-0050.reason", "Lorem ipsum"); // Will result in 1 exemption
		c3.setProperty(ExemptionBug.CFG_PREFIX + ".CVE-2014-0050.libraries", "6F1EBC6CE20AD8B3D4825CEB2E625E5C432A0E10");
		final ExemptionSet e3 = ExemptionSet.createFromConfiguration(c3.getConfiguration());
		assertEquals(1, e3.size());

		final VulasConfiguration c4 = new VulasConfiguration();
		c4.setProperty(ExemptionBug.CFG_PREFIX + ".CVE-2014-0050.reason", "Lorem ipsum"); // Will result in 1 exemption
		final ExemptionSet e4 = ExemptionSet.createFromConfiguration(c4.getConfiguration());
		assertEquals(1, e4.size());
		
		final VulasConfiguration c5 = new VulasConfiguration();
		c5.setProperty(ExemptionBug.CFG_PREFIX + ".CVE-2014-0050.reason", "Lorem ipsum"); // Will result in 2 exemption
		c5.setProperty(ExemptionBug.CFG_PREFIX + ".CVE-2014-0050.libraries", "123, pkg:maven/stuff/stuff@stuff, abc");
		final ExemptionSet e5 = ExemptionSet.createFromConfiguration(c5.getConfiguration());
		assertEquals(3, e5.size());

		try {
			// digest = 6F1EBC6CE20AD8B3D4825CEB2E625E5C432A0E10, bugId = CVE-2014-0050, scope = SYSTEM, cvssScore = 7.5, wellknownDigest = false, affected_version_confirmed = 0
			final VulnerableDependency vd = (VulnerableDependency)JacksonUtil.asObject(FileUtil.readFile("./src/test/resources/vulndep.json"), VulnerableDependency.class);
			vd.setAboveThreshold(false);

			// Exempt bug and serialize
			vd.setExemption(e1.getApplicableExemption(vd));
			assertTrue(vd.getExemption()!=null);
			String serialized_vd = JacksonUtil.asJsonString(vd);
			assertTrue(serialized_vd!=null && serialized_vd.indexOf("\"exemption\":{\"bugId\":\"CVE-2014-0050\",\"library\":\"*\",\"reason\":\"No reason provided\"}")!=-1);

			// Exempt scope and serialize
			vd.setExemption(e2.getApplicableExemption(vd));
			assertTrue(vd.getExemption()!=null);
			serialized_vd = JacksonUtil.asJsonString(vd);
			assertTrue(serialized_vd!=null && serialized_vd.indexOf("\"exemption\":{\"reason\":\"Vulnerable dependencies with scope [SYSTEM] are exempted through configuration settings [vulas.report.exemptScope] or [vulas.report.exceptionScopeBlacklist] (deprecated)\"}")!=-1);

			// Exempt bug per digest and serialize
			vd.setExemption(e3.getApplicableExemption(vd));
			assertTrue(vd.getExemption()!=null);
			serialized_vd = JacksonUtil.asJsonString(vd);
			assertTrue(serialized_vd!=null && serialized_vd.indexOf("\"exemption\":{\"bugId\":\"CVE-2014-0050\",\"library\":\"6F1EBC6CE20AD8B3D4825CEB2E625E5C432A0E10\",\"reason\":\"Lorem ipsum\"}")!=-1);

			// Exempt bug and serialize
			vd.setExemption(e4.getApplicableExemption(vd));
			assertTrue(vd.getExemption()!=null);
			serialized_vd = JacksonUtil.asJsonString(vd);
			assertTrue(serialized_vd!=null && serialized_vd.indexOf("\"exemption\":{\"bugId\":\"CVE-2014-0050\",\"library\":\"*\",\"reason\":\"Lorem ipsum\"}")!=-1);
			
			// Exempt bug and serialize
			vd.setExemption(e5.getApplicableExemption(vd));
			assertTrue(vd.getExemption()!=null);
			serialized_vd = JacksonUtil.asJsonString(vd);
			assertTrue(serialized_vd!=null && serialized_vd.indexOf("\"exemption\":{\"bugId\":\"CVE-2014-0050\",\"library\":\"pkg:maven/stuff/stuff@stuff\",\"reason\":\"Lorem ipsum\"}")!=-1);
		} catch (IOException ioe) {
			ioe.printStackTrace();
			assertTrue(false);
		}
	}
	
	@Test
	public void testCreatePackageUrl() {
		// type == maven
		String purl = "pkg:maven/a@v";
		try {
			ExemptionBug.createPackageUrl(purl);
			assertTrue(false);
		} catch(MalformedPackageURLException e) {
			assertTrue(true);
			assertEquals("Package URLs of type [maven] require a valid namespace and name: [" + purl + "]",  e.getMessage());
		}
		
		try {
			ExemptionBug.createPackageUrl("pkg:maven/g/@v");
			assertTrue(false);
		} catch(MalformedPackageURLException e) {
			assertTrue(true);
		}
	
		// type == pypi
		try {
			ExemptionBug.createPackageUrl("pkg:pypi/a@v");
			assertTrue(true);
		} catch(MalformedPackageURLException e) { 
			assertTrue(false);
		}

		// other unsupported type(s)
		purl = "pkg:npm/g/a@v";
		try {
			ExemptionBug.createPackageUrl(purl);
			assertTrue(false);
		} catch(MalformedPackageURLException e) {
			assertTrue(true);
			assertEquals("Package URLs of type [npm] are not supported: [" + purl + "]", e.getMessage());
		}
	}
}