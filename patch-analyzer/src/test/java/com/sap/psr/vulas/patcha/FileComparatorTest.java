/**
 * This file is part of Eclipse Steady.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 */
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
}