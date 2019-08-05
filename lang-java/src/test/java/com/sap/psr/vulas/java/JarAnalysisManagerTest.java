package com.sap.psr.vulas.java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.file.Paths;
import java.util.Set;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.sap.psr.vulas.shared.categories.Slow;
import com.sap.psr.vulas.shared.util.FileSearch;

public class JarAnalysisManagerTest {

	@Test
	@Category(Slow.class)
	public void testStartAnalysis() {
		try {
			final ArchiveAnalysisManager jam = new ArchiveAnalysisManager(4, -1, false, null);
			final FileSearch fs = new FileSearch(new String[] { "war" });
			jam.startAnalysis(fs.search(Paths.get("./src/test/resources")), null);
			final Set<JarAnalyzer> analyzers = jam.getAnalyzers();
			assertEquals(16, analyzers.size()); // 1 for the WAR and 15 for JARs in WEB-INF/lib
		} catch(Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
}
