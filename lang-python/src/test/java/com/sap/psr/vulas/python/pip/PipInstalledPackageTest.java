package com.sap.psr.vulas.python.pip;

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