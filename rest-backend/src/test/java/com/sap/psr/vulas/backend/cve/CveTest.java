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
