package com.sap.psr.vulas.java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Paths;
import java.util.Set;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.sap.psr.vulas.ConstructId;
import com.sap.psr.vulas.FileAnalyzer;
import com.sap.psr.vulas.core.util.CoreConfiguration;
import com.sap.psr.vulas.shared.categories.Slow;
import com.sap.psr.vulas.shared.json.model.Application;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

public class WarAnalyzerTest {

	@Test
	@Category(Slow.class)
	public void testAnalyze() {
		try {
			final WarAnalyzer wa = new WarAnalyzer();
			wa.analyze(new File("./src/test/resources/examples.war"));
			wa.setWorkDir(Paths.get("./target"));
			wa.setRename(true);
			WarAnalyzer.setAppContext(new Application("dummy-group", "dummy-artifact", "0.0.1-SNAPSHOT"));
			wa.call();
			
			// 15 archives in WEB-INF/lib
			final Set<FileAnalyzer> fas = wa.getChilds(true);
			assertEquals(15, fas.size());	
			
			// 15 constructs in classes in WEB-INF/classes
			final Set<ConstructId> cids = wa.getConstructIds();
			assertEquals(15, cids.size());			
		} catch(Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	@Test
	@Category(Slow.class)
	public void testInstrument() {
		try {
			VulasConfiguration.getGlobal().setProperty(CoreConfiguration.INSTR_WRITE_CODE, "true");
			final WarAnalyzer wa = new WarAnalyzer();
			wa.analyze(new File("./src/test/resources/examples.war"));
			wa.setWorkDir(Paths.get("./target"));
			wa.setRename(true);
			wa.setInstrument(true);
			WarAnalyzer.setAppContext(new Application("dummy-group", "dummy-artifact", "0.0.1-SNAPSHOT"));
			wa.call();
			
			// Check instrumented WAR
			final File new_war = wa.getInstrumentedArchive();
			assertTrue(new_war.exists());
		} catch(Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
}
