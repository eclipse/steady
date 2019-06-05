package com.sap.psr.vulas.java;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Set;

import org.junit.Test;

import com.sap.psr.vulas.ConstructId;
import com.sap.psr.vulas.FileAnalysisException;

public class AarAnalyzerTest {

	@Test
	public void testAarAnalyzer() {
		try {
			final AarAnalyzer aa = new AarAnalyzer();
			aa.analyze(new File("./src/test/resources/cucumber-android-4.3.0.aar"));
			final Set<ConstructId> cids = aa.getConstructIds();
			assertTrue(!cids.isEmpty());
		} catch (FileAnalysisException e) {
			e.printStackTrace();
			assertTrue(false);
		}		
	}
}