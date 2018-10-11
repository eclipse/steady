package com.sap.psr.vulas.shared.model.generic;

import static org.junit.Assert.*;

import org.junit.Test;

import com.sap.psr.vulas.shared.json.model.Version;

public class VersionTest {

	@Test
	public void test() {
		Version v0 = new Version ("2.2.0");
		Version v1 = new Version ("2.2.0.1");
		Version v2 = new Version ("2.2.1");
		Version v3 = new Version ("2.2.1.1");
		
		assertTrue(v0.compareTo(v1)<0);
		assertTrue(v1.compareTo(v0)>0);
		assertTrue(v2.compareTo(v1)>0);
		assertTrue(v3.compareTo(v1)>0);
		
	}

}
