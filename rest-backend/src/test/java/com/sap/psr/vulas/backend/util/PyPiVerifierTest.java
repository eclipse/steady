package com.sap.psr.vulas.backend.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import com.sap.psr.vulas.shared.util.FileUtil;

public class PyPiVerifierTest {

	@Test
	public void testVerify() throws IOException {
		final String json = FileUtil.readFile("./src/test/resources/pypi_flask.json");
		
		PyPiVerifier ppv = new PyPiVerifier();
		assertTrue(ppv.containsMD5(json,  "c1d30f51cff4a38f9454b23328a15c5a")); // Flask 0.11
		assertEquals(1482348132000L, ppv.getReleaseTimestamp().longValue());
		
		ppv = new PyPiVerifier();
		assertFalse(ppv.containsMD5(json, "c1d30f51cff4a38f9454b23328a15c5azzzzz")); // Does not exist
		assertEquals(null, ppv.getReleaseTimestamp());
	}
}
