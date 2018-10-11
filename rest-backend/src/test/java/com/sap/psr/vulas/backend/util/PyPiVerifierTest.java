package com.sap.psr.vulas.backend.util;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Paths;

import org.aspectj.util.FileUtil;
import org.junit.Test;

public class PyPiVerifierTest {

	@Test
	public void testVerify() throws IOException {
		final String json = FileUtil.readAsString(Paths.get("./src/test/resources/pypi_flask.json").toFile());
		assertTrue(PyPiVerifier.containsMD5(json,  "c1d30f51cff4a38f9454b23328a15c5a"));
	}
}
