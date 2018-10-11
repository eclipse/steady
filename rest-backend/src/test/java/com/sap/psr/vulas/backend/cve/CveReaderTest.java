package com.sap.psr.vulas.backend.cve;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.sap.psr.vulas.shared.cache.CacheException;
import com.sap.psr.vulas.shared.categories.RequiresNetwork;

public class CveReaderTest {

	//@Test
	//@Category(RequiresNetwork.class)
	public void testRead() throws CacheException {
		
		for(int i=0; i<10; i++) {
			final Cve cve_0050 = CveReader.read("CVE-2014-0050");
			final Cve cve_00xx = CveReader.read("CVE-2014-00xx"); // does not exist
		}
	}
}
