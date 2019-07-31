package com.sap.psr.vulas.patcha;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.junit.Test;

import com.sap.psr.vulas.ConstructChange;
import com.sap.psr.vulas.FileAnalysisException;
import com.sap.psr.vulas.patcha.FileComparator;

public class FileComparatorTest {

	/**
	 * Compares two files from CXF, related to CVE-2013-0239.
	 * The fix consisted of modifying one method and adding a new one.
	 * Altogether, there should be 4 changes, including package and class.
	 */
	@Test
	public void testFileComparatorOnJava() {
		// Create instance of FileComparator
		try {
			final FileComparator c = new FileComparator(new File("./src/test/resources/ws_security_1438423/src/main/java/org/apache/cxf/ws/security/wss4j/UsernameTokenInterceptor.java"),
					new File("./src/test/resources/ws_security_1438424/src/main/java/org/apache/cxf/ws/security/wss4j/UsernameTokenInterceptor.java"), null, null, null, null);
			final Set<ConstructChange> changes = c.identifyChanges();
			for(ConstructChange chg : changes)
				System.out.println(chg);
			assertEquals(4, changes.size());
		} catch (IOException e) {
			System.err.println(e.getMessage());
			assertTrue(false);
		} catch (FileAnalysisException e) {
			System.err.println(e.getMessage());
			assertTrue(false);
		}
	}
	
	/**
	 * Compares two files from Flask OIDS, related to CVE-2016-1000001.
	 * The fix consisted of modifying three methods.
	 * Altogether, there should be 5 changes, including module and class.
	 */
	@Test
	public void testFileComparatorOnPy() {
		// Create instance of FileComparator
		try {
			final FileComparator c = new FileComparator(new File("./src/test/resources/flask-oidc_e4ce5/flask_oidc/__init__.py"),
					new File("./src/test/resources/flask-oidc_f2ef8/flask_oidc/__init__.py"), null, null, null, null);
			final Set<ConstructChange> changes = c.identifyChanges();
			for(ConstructChange chg : changes)
				System.out.println(chg);
			assertEquals(5, changes.size());
		} catch (IOException e) {
			System.err.println(e.getMessage());
			assertTrue(false);
		} catch (FileAnalysisException e) {
			System.err.println(e.getMessage());
			assertTrue(false);
		}
	}

	/**
	 * Compares two files from lodash, related to CVE-2019-10744
	 * The fix consisted of modifying one function.
	 * Note that this is only the example from one file.
	 * Altogether, there should be 4 changes, including module, class and outer function.
	 */
	@Test
	public void testFileComparatorOnJs() {
		// Create instance of FileComparator
		try {
			final FileComparator c = new FileComparator(new File("./src/test/resources/lodash-lodash-e42cd97/lodash/lodash.js"),
					new File("./src/test/resources/lodash-lodash-1f8ea07/lodash/lodash.js"), null, null, null, null);
			final Set<ConstructChange> changes = c.identifyChanges();
			for(ConstructChange chg : changes)
				System.out.println(chg);
			assertEquals(4, changes.size());
		} catch (IOException e) {
			System.err.println(e.getMessage());
			assertTrue(false);
		} catch (FileAnalysisException e) {
			System.err.println(e.getMessage());
			assertTrue(false);
		}
	}
}